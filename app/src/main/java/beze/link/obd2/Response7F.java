package beze.link.obd2;

import android.location.Address;

import java.lang.reflect.Parameter;
import java.security.Security;
import java.util.function.Function;
import java.util.zip.Checksum;

public class Response7F {

    private boolean valid;
    private int responseByte;

    public static class Responses {
        public static final int Affirmative = 0;
        public static final int GeneralReject = 0x10;
        public static final int ModeNotSupported = 0x11;
        public static final int SubFunctionNotSupportedOrInvalidFormat = 0x12;
        public static final int BusyRepeatRequest = 0x21;
        public static final int ConditionsNotCorrectOrRequestSequenceError = 0x22;
        public static final int RoutineNotComplete = 0x23;
        public static final int RequestOutOfRange = 0x31;
        public static final int SecurityAccessDenied = 0x33;
        public static final int SecurityAccessAllowed = 0x34;
        public static final int InvalidKey = 0x35;
        public static final int ExceedNumberOfAttempts = 0x36;
        public static final int RequiredTimeDelayNotExpired = 0x37;
        public static final int DownloadNotAccepted = 0x40;
        public static final int ImproperDownloadType = 0x41;
        public static final int CannotDownloadToSpecifiedAddress = 0x42;
        public static final int CannotDownloadNumberOfBytesRequested = 0x43;
        public static final int ReadyForDownload = 0x44;
        public static final int UploadNotAccepted = 0x50;
        public static final int ImproperUploadType = 0x51;
        public static final int CannotUploadFromSpecifiedAddress = 0x52;
        public static final int CannotUploadNumberOfBytesRequested = 0x53;
        public static final int ReadyForUpload = 0x54;
        public static final int NormalExitWithResultsAvailable = 0x61;
        public static final int NormalExitWithoutResultsAvailable = 0x62;
        public static final int AbnormalExitWithResults = 0x63;
        public static final int AbnormalExitWithoutResults = 0x64;
        public static final int TransferSuspended = 0x71;
        public static final int TransferAborted = 0x72;
        public static final int BlockTransferCompleteNextBLock = 0x73;
        public static final int IllegalAddressInBlockTransfer = 0x74;
        public static final int IllegalByteCountInBlockTransfer = 0x75;
        public static final int IllegalBlockTranfserType = 0x76;
        public static final int BlockTransferDataChecksumError = 0x77;
        public static final int BlockTransferMessageCorrectlyReceived = 0x78;
        public static final int IncorrectByteCountDuringBlockTransfer = 0x79;
    }

    public Response7F(int[] values) {

        // need at least 4 bytes to have a valid response, e.g. "7F 22 15 12"
        if (values == null || values.length <= 3) {
            valid = false;
            responseByte = -1;
        }
        else {
            valid           = values[0] == 0x7F;
            responseByte    = values[values.length - 1];
        }
    }

    public boolean isValid() {
        return valid;
    }

}
