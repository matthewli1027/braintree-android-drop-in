package com.braintreepayments.demo.test;

import android.support.test.filters.RequiresDevice;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.widget.Button;

import com.braintreepayments.demo.Settings;
import com.braintreepayments.demo.test.utilities.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static com.braintreepayments.demo.test.utilities.CardNumber.UNIONPAY_CREDIT;
import static com.braintreepayments.demo.test.utilities.CardNumber.UNIONPAY_SMS_NOT_REQUIRED;
import static com.braintreepayments.demo.test.utilities.CardNumber.VISA;
import static com.lukekorth.deviceautomator.AutomatorAction.click;
import static com.lukekorth.deviceautomator.AutomatorAction.setText;
import static com.lukekorth.deviceautomator.AutomatorAssertion.text;
import static com.lukekorth.deviceautomator.DeviceAutomator.onDevice;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withContentDescription;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withText;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withTextContaining;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withTextStartingWith;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

@RunWith(AndroidJUnit4.class)
public class DropInTest extends TestHelper {

    @Before
    public void setup() {
        super.setup();
        onDevice(withText("Drop-In")).waitForEnabled().perform(click());
    }

    @Test(timeout = 60000)
    public void tokenizesACard() {
        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withContentDescription("Card Number")).perform(setText(VISA));
        onDevice(withText("12")).perform(click());
        onDevice(withText("2019")).perform(click());
        onDevice().pressBack();
        onDevice(withContentDescription("CVV")).perform(setText("123"));
        onDevice(withContentDescription("Postal Code")).perform(setText("12345"));
        onDevice(withTextContaining("Add Card")).perform(click());

        getNonceDetails().check(text(containsString("Card Last Two: 11")));

        onDevice(withText("Create a Transaction")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test
    public void tokenizesUnionPay() {
        onDevice().pressBack();
        setMerchantAccountId(Settings.getUnionPayMerchantAccountId(getTargetContext()));
        onDevice(withText("Drop-In")).waitForEnabled().perform(click());

        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withContentDescription("Card Number")).perform(setText(UNIONPAY_CREDIT));
        onDevice(withText("12")).perform(click());
        onDevice(withText("2019")).perform(click());
        onDevice().pressBack();
        onDevice(withContentDescription("CVN")).perform(setText("123"));
        onDevice(withContentDescription("Postal Code")).perform(setText("12345"));
        onDevice(withContentDescription("Country Code")).perform(setText("01"));
        onDevice(withContentDescription("Mobile Number")).perform(setText("5555555555"));
        onDevice(withTextContaining("Add Card")).perform(click());
        onDevice(withContentDescription("SMS Code")).perform(setText("12345"));
        onDevice(withTextContaining("Confirm", Button.class)).perform(click());

        getNonceDetails().check(text(containsString("Card Last Two: 32")));

        onDevice(withText("Create a Transaction")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test
    public void tokenizesUnionPayWhenEnrollmentIsNotRequired() {
        onDevice().pressBack();
        setMerchantAccountId(Settings.getUnionPayMerchantAccountId(getTargetContext()));
        onDevice(withText("Drop-In")).waitForEnabled().perform(click());

        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withContentDescription("Card Number")).perform(setText(UNIONPAY_SMS_NOT_REQUIRED));
        onDevice(withText("12")).perform(click());
        onDevice(withText("2019")).perform(click());
        onDevice().pressBack();
        onDevice(withContentDescription("CVN")).perform(setText("123"));
        onDevice(withContentDescription("Postal Code")).perform(setText("12345"));
        onDevice(withContentDescription("Country Code")).perform(setText("01"));
        onDevice(withContentDescription("Mobile Number")).perform(setText("5555555555"));
        onDevice(withTextContaining("Add Card")).perform(click());

        getNonceDetails().check(text(containsString("Card Last Two: 85")));

        onDevice(withText("Create a Transaction")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @SdkSuppress(minSdkVersion = 21)
    @Test(timeout = 60000)
    public void tokenizesPayPal() {
        uninstallPayPalWallet();

        onDevice(withText("PayPal")).perform(click());
        onDevice(withContentDescription("Proceed with Sandbox Purchase")).perform(click());

        getNonceDetails().check(text(containsString("Email: bt_buyer_us@paypal.com")));

        onDevice(withText("Create a Transaction")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @RequiresDevice
    @Test(timeout = 60000)
    public void tokenizesAndroidPay() {
        onDevice(withText("Android Pay")).perform(click());
        onDevice(withText("CONTINUE")).perform(click());

        getNonceDetails().check(text(containsString("Underlying Card Last Two")));

        onDevice(withText("Create a Transaction")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test(timeout = 60000)
    public void exitsAfterCancelingAddingAPaymentMethod() {
        onDevice(withText("PayPal")).perform(click());
        onDevice(withContentDescription("Proceed with Sandbox Purchase")).waitForExists();
        onDevice().pressBack();
        onDevice(withContentDescription("Pay with PayPal")).waitForExists();

        onDevice().pressBack();

        onDevice(withText("Drop-In")).check(text(equalToIgnoringCase("Drop-In")));
    }
}
