package com.passgo.app.feature.vault

import android.net.Uri
import com.passgo.app.core.error.AppException
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.model.Attachment
import com.passgo.app.data.repository.AttachmentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AttachmentViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var attachmentRepository: AttachmentRepository
    private lateinit var passGoLogger: PassGoLogger
    private lateinit var viewModel: AttachmentViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        attachmentRepository = mockk()
        passGoLogger = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty not loading no error`() {
        every { attachmentRepository.getAttachmentsForItem(any()) } returns flowOf(emptyList())

        viewModel = AttachmentViewModel(attachmentRepository, passGoLogger)

        assertTrue(viewModel.attachments.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertFalse(viewModel.isAdding.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadAttachments populates list`() = runTest(testDispatcher) {
        val items = listOf(
            Attachment(id = "a1", itemId = "item1", name = "doc.pdf", sizeBytes = 1024)
        )
        every { attachmentRepository.getAttachmentsForItem("item1") } returns flowOf(items)

        viewModel = AttachmentViewModel(attachmentRepository, passGoLogger)

        val job = launch { viewModel.attachments.collect { } }
        viewModel.loadAttachments("item1")
        advanceUntilIdle()

        assertEquals(1, viewModel.attachments.value.size)
        assertEquals("doc.pdf", viewModel.attachments.value.first().name)
        job.cancel()
    }

    @Test
    fun `loadAttachments shows loading state`() = runTest(testDispatcher) {
        every { attachmentRepository.getAttachmentsForItem(any()) } returns flowOf(emptyList())

        viewModel = AttachmentViewModel(attachmentRepository, passGoLogger)

        val job = launch { viewModel.attachments.collect { } }
        viewModel.loadAttachments("item1")
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        job.cancel()
    }

    @Test
    fun `addAttachment calls repository`() = runTest(testDispatcher) {
        every { attachmentRepository.getAttachmentsForItem(any()) } returns flowOf(emptyList())
        coEvery { attachmentRepository.addAttachment(any(), any(), any(), any()) } returns
            AppResult.Success(
                Attachment(id = "a1", itemId = "item1", name = "test.pdf")
            )

        viewModel = AttachmentViewModel(attachmentRepository, passGoLogger)

        val job = launch { viewModel.attachments.collect { } }
        viewModel.loadAttachments("item1")
        advanceUntilIdle()

        viewModel.addAttachment(
            mockk(),
            "test.pdf",
            "application/pdf"
        )
        advanceUntilIdle()

        coVerify { attachmentRepository.addAttachment(any(), "item1", "test.pdf", "application/pdf") }
        assertFalse(viewModel.isAdding.value)
        job.cancel()
    }

    @Test
    fun `addAttachment handles error`() = runTest(testDispatcher) {
        every { attachmentRepository.getAttachmentsForItem(any()) } returns flowOf(emptyList())
        coEvery { attachmentRepository.addAttachment(any(), any(), any(), any()) } returns
            AppResult.Error(AppException.UnknownException("Failed"))

        viewModel = AttachmentViewModel(attachmentRepository, passGoLogger)

        viewModel.loadAttachments("item1")
        advanceUntilIdle()

        assertNull(viewModel.error.value)

        viewModel.addAttachment(
            mockk(),
            "test.pdf",
            "application/pdf"
        )
        advanceUntilIdle()

        assertEquals("Failed to add attachment", viewModel.error.value)
        assertFalse(viewModel.isAdding.value)
    }

    @Test
    fun `deleteAttachment calls repository`() = runTest(testDispatcher) {
        every { attachmentRepository.getAttachmentsForItem(any()) } returns flowOf(emptyList())
        coEvery { attachmentRepository.deleteAttachmentPermanently(any()) } returns
            AppResult.Success(Unit)

        viewModel = AttachmentViewModel(attachmentRepository, passGoLogger)

        viewModel.loadAttachments("item1")
        advanceUntilIdle()

        viewModel.deleteAttachment("a1")
        advanceUntilIdle()

        coVerify { attachmentRepository.deleteAttachmentPermanently("a1") }
    }

    @Test
    fun `deleteAttachment handles error`() = runTest(testDispatcher) {
        every { attachmentRepository.getAttachmentsForItem(any()) } returns flowOf(emptyList())
        coEvery { attachmentRepository.deleteAttachmentPermanently(any()) } returns
            AppResult.Error(AppException.UnknownException("Failed"))

        viewModel = AttachmentViewModel(attachmentRepository, passGoLogger)

        viewModel.loadAttachments("item1")
        advanceUntilIdle()

        assertNull(viewModel.error.value)

        viewModel.deleteAttachment("a1")
        advanceUntilIdle()

        assertEquals("Failed to delete attachment", viewModel.error.value)
    }

    @Test
    fun `clearError resets error state`() = runTest(testDispatcher) {
        every { attachmentRepository.getAttachmentsForItem(any()) } returns flowOf(emptyList())
        coEvery { attachmentRepository.addAttachment(any(), any(), any(), any()) } returns
            AppResult.Error(AppException.UnknownException("Failed"))

        viewModel = AttachmentViewModel(attachmentRepository, passGoLogger)

        viewModel.loadAttachments("item1")
        advanceUntilIdle()

        viewModel.addAttachment(
            mockk(),
            "test.pdf",
            "application/pdf"
        )
        advanceUntilIdle()

        assertEquals("Failed to add attachment", viewModel.error.value)

        viewModel.clearError()
        assertNull(viewModel.error.value)
    }

    @Test
    fun `isAdding state during add`() = runTest(testDispatcher) {
        every { attachmentRepository.getAttachmentsForItem(any()) } returns flowOf(emptyList())
        coEvery { attachmentRepository.addAttachment(any(), any(), any(), any()) } returns
            AppResult.Success(
                Attachment(id = "a1", itemId = "item1", name = "test.pdf")
            )

        viewModel = AttachmentViewModel(attachmentRepository, passGoLogger)

        viewModel.loadAttachments("item1")
        advanceUntilIdle()

        viewModel.addAttachment(
            mockk(),
            "test.pdf",
            "application/pdf"
        )
        advanceUntilIdle()

        assertFalse(viewModel.isAdding.value)
    }
}
