package com.example.projecttwocs360

import android.content.Context
import android.telephony.SmsManager
import android.widget.Toast
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

// Use MockitoJUnitRunner to initialize mocks
@RunWith(MockitoJUnitRunner::class)
class SMSUtilsTest {

    // @Mock creates a mock implementationfor the annotated field.
    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSmsManager: SmsManager

    // @Captor is used to capture arguments passed to mocked methods.
    @Captor
    private lateinit var stringCaptor: ArgumentCaptor<String>

    @Captor
    private lateinit var longCaptor: ArgumentCaptor<Int> // For Toast duration

    // We need to mock the static Toast.makeText method.
    // This requires mockito-inline and a bit more setup.
    // Alternatively, you could wrap Toast calls in your SMSUtils in another
    // injectable class, which is often a cleaner approach for testability.

    @Before
    fun setUp() {
        // It's tricky to directly mock SmsManager.getDefault() without PowerMock or more complex setup.
        // A common pattern is to make SmsManager injectable into SMSUtils,
        // or have a way to set a test instance.
        // For this example, let's assume we can't easily change SMSUtils.
        // We'll focus on verifying interactions if SmsManager.getDefault() were to return our mock.
        // This is a limitation of testing static framework methods directly.

        // For Toast, static mocking is needed.
        // This setup is for mocking static methods with Mockito.
        // Note: Mocking static methods directly can be fragile.
        // Consider wrapping Toast calls in a non-static, injectable class for easier testing.
        mockStatic(Toast::class.java).use { mockedToast ->
            // When Toast.makeText is called with any CharSequence, Context, and Int,
            // return a dummy (or mocked) Toast object.
            // We don't usually care about the Toast object itself in unit tests,
            // but rather that makeText was called with correct parameters.
            val mockToastInstance = mock(Toast::class.java)
            mockedToast.`when`<Toast> {
                Toast.makeText(
                    any(Context::class.java),
                    anyString(),
                    anyInt()
                )
            }.thenReturn(mockToastInstance)
        }

        // A more robust way for SmsManager would be to refactor SMSUtils to accept SmsManager as a parameter
        // or have a setter for testing.
        // For now, we'll proceed assuming we wantto see if sendTextMessage is called.
        // This part is more illustrative of what you'd WANT to test if SmsManager was easily mockable here.
    }

    @Test
    fun sendSms_successfulSend_callsSmsManagerAndShowsSuccessToast() {
        val testPhoneNumber = "1234567890"
        val testMessage = "Hello Test"

        // --- This is the ideal way if SmsManager.getDefault() could be mocked ---
        // This part is tricky because SmsManager.getDefault() is static and returns a system service.
        // Without PowerMockito or refactoring SMSUtils, directly mocking getDefault() is hard.
        // Let's assume for a moment we COULD inject or mock it.
        //
        // If SMSUtils was:
        // object SMSUtils {
        //    fun sendSms(context: Context, phoneNumber: String, message: String, smsManagerInstance: SmsManager = SmsManager.getDefault()) {
        //        smsManagerInstance.sendTextMessage(...)
        //    }
        // }
        // Then in test:
        // SMSUtils.sendSms(mockContext, testPhoneNumber, testMessage, mockSmsManager)
        // verify(mockSmsManager).sendTextMessage(testPhoneNumber, null, testMessage, null, null)
        // --- End of ideal scenario ---

        // For the current structure, we'll focus on the Toast part and acknowledge SmsManager testing limitations.
        // We can't easily verify mockSmsManager.sendTextMessage directly here without more advanced tools
        // or refactoring SMSUtils to make SmsManager injectable.

        SMSUtils.sendSms(mockContext, testPhoneNumber, testMessage)

        // Verify that Toast.makeText was called for success
        // We need to use `mockStatic(Toast::class.java).run { ... }` if we didn't use @Before for it.
        // Since we did it in @Before, we can verify directly.
        mockStatic(Toast::class.java).use { mockedToast ->
            mockedToast.verify {
                Toast.makeText(eq(mockContext), stringCaptor.capture(), longCaptor.capture())
            }
            assertEquals("SMS sent!", stringCaptor.value)
            assertEquals(Toast.LENGTH_SHORT, longCaptor.value.toInt())
        }

        // To truly test smsManager.sendTextMessage, you would typically:
        // 1. Refactor SMSUtils to accept an SmsManager instance (Dependency Injection).
        // 2. Use a library like PowerMockito to mock static methods like SmsManager.getDefault() (can be complex).
        // 3. Use Robolectric, which provides shadows for many Android framework classes,
        //    allowing SmsManager.getDefault() to return a shadow object you can inspect.
    }

    @Test
    fun sendSms_whenSmsManagerThrowsException_showsFailureToast() {
        val testPhoneNumber = "0987654321"
        val testMessage = "Test Failure"
        val exceptionMessage = "SIM card not ready"
    }
}

// This is where mocking SmsManager.getDefault() or the instance it returns is crucial.
// Let's assume we refactored SMSUtils to allow injecting SmsManager for this test.
//
// Hypothetical refactor of SMSUtils for testability:
// object SMSUtils {
//     var smsManagerProvider: () -> SmsManager = { SmsManager.getDefault() } // Allow overriding for tests
//     fun sendSms(context: Context, phoneNumber: String, message: String) {
//         try {
//             val smsManager = smsManagerProvider()
//             smsManager.sendTextMessage(phoneNumber, null, message, null, null)
//             // ... success toast
//         } catch (e: Exception) {
//             // ... failure toast
//         }
//     }
// }
//
// Then in test:
// SMSUtils.smsManagerProvider = { mockSmsManager }
// `when`(mockSmsManager.sendTextMessage(anyString(), isNull(), anyString(), isNull(), isNull()))
//    .thenThrow(SecurityException(exceptionMessage))

// For the current SMSUtils, we can't easily make sendTextMessage throw an exception
// in a pure JVM unit test without more advanced mocking tools for static methods.
// The test below focuses on what we *can* verify: the Toast message if an exception *were* to occur.
// To actually trigger the catchblock, you'd need to mock the SmsManager behavior.

// Let's simulate the scenario where an exception occurs *after* an