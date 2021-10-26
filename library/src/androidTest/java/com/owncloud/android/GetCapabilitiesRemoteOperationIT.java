/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2019 Tobias Kaminsky
 *   Copyright (C) 2019 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */
package com.owncloud.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.status.CapabilityBooleanType;
import com.owncloud.android.lib.resources.status.E2EVersion;
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation;
import com.owncloud.android.lib.resources.status.NextcloudVersion;
import com.owncloud.android.lib.resources.status.OCCapability;
import com.owncloud.android.lib.resources.status.OwnCloudVersion;

import org.junit.Test;

/**
 * Class to test GetRemoteCapabilitiesOperation
 */
public class GetCapabilitiesRemoteOperationIT extends AbstractIT {
    /**
     * Test get capabilities
     */
    @Test
    public void testGetRemoteCapabilitiesOperation() {
        // get capabilities
        RemoteOperationResult result = new GetCapabilitiesRemoteOperation().execute(client);
        assertTrue(result.isSuccess());
        assertTrue(result.getData() != null && result.getData().size() == 1);

        OCCapability capability = (OCCapability) result.getData().get(0);
        checkCapability(capability, client.getUserId());
    }

    @Test
    public void testGetRemoteCapabilitiesOperationEtag() {
        // get capabilities
        RemoteOperationResult result = new GetCapabilitiesRemoteOperation().execute(client);
        assertTrue(result.isSuccess());
        assertTrue(result.getData() != null && result.getData().size() == 1);

        OCCapability capability = (OCCapability) result.getData().get(0);

        RemoteOperationResult resultEtag = new GetCapabilitiesRemoteOperation(capability).execute(client);
        assertTrue(resultEtag.isSuccess());
        assertTrue(resultEtag.getData() != null && resultEtag.getData().size() == 1);

        OCCapability sameCapability = (OCCapability) resultEtag.getData().get(0);

        if (capability.getVersion().isNewerOrEqual(OwnCloudVersion.nextcloud_19)) {
            assertEquals(capability, sameCapability);
        } else {
            assertEquals(capability.getEtag(), sameCapability.getEtag());
        }

        checkCapability(capability, client.getUserId());
    }

    /**
     * Test get capabilities
     */
    @Test
    public void testGetRemoteCapabilitiesOperationWithNextcloudClient() {
        // get capabilities
        RemoteOperationResult result = new GetCapabilitiesRemoteOperation().execute(nextcloudClient);
        assertTrue(result.isSuccess());
        assertTrue(result.getData() != null && result.getData().size() == 1);

        OCCapability capability = (OCCapability) result.getData().get(0);
        checkCapability(capability, client.getUserId());
    }

    @Test
    public void testGetRemoteCapabilitiesOperationEtagWithNextcloudClient() {
        // get capabilities
        RemoteOperationResult result = new GetCapabilitiesRemoteOperation().execute(nextcloudClient);
        assertTrue(result.isSuccess());
        assertTrue(result.getData() != null && result.getData().size() == 1);

        OCCapability capability = (OCCapability) result.getData().get(0);

        RemoteOperationResult resultEtag = new GetCapabilitiesRemoteOperation(capability).execute(nextcloudClient);
        assertTrue(resultEtag.isSuccess());
        assertTrue(resultEtag.getData() != null && resultEtag.getData().size() == 1);

        OCCapability sameCapability = (OCCapability) resultEtag.getData().get(0);

        if (capability.getVersion().isNewerOrEqual(OwnCloudVersion.nextcloud_19)) {
            assertEquals(capability, sameCapability);
        } else {
            assertEquals(capability.getEtag(), sameCapability.getEtag());
        }

        checkCapability(capability, nextcloudClient.getUserId());
    }

    @Test
    public void testFilesSharing() {
        // get capabilities
        RemoteOperationResult result = new GetCapabilitiesRemoteOperation().execute(nextcloudClient);
        assertTrue(result.isSuccess());
        assertTrue(result.getData() != null && result.getData().size() == 1);

        OCCapability capability = (OCCapability) result.getData().get(0);

        // share by mail
        if (capability.getVersion().isNewerOrEqual(NextcloudVersion.nextcloud_23)) {
            assertTrue(capability.getFilesSharingByMail().isTrue());
            assertTrue(capability.getFilesSharingByMailSendPasswordByMail().isTrue());
        } else {
            assertTrue("Value is:" + capability.getFilesSharingByMail(), capability.getFilesSharingByMail().isTrue());
            assertTrue(capability.getFilesSharingByMailSendPasswordByMail().isUnknown());
        }
    }

    private void checkCapability(OCCapability capability, String userId) {
        assertTrue(capability.getActivity().isTrue());
        assertTrue(capability.getFilesSharingApiEnabled().isTrue());
        assertTrue(capability.getFilesVersioning().isTrue());
        assertTrue(capability.getFilesUndelete().isTrue());
        assertNotNull(capability.getVersion());
        assertFalse(capability.getEtag().isEmpty());
        assertSame(CapabilityBooleanType.FALSE, capability.getRichDocuments());
        assertFalse(capability.getDirectEditingEtag().isEmpty());
        assertSame(CapabilityBooleanType.UNKNOWN, capability.getDropAccount());

        // user status
        if (capability.getVersion().isNewerOrEqual(OwnCloudVersion.nextcloud_20)) {
            assertTrue(capability.getUserStatus().isTrue());
            assertTrue(capability.getUserStatusSupportsEmoji().isTrue());
        } else {
            assertFalse(capability.getUserStatus().isTrue());
            assertFalse(capability.getUserStatusSupportsEmoji().isTrue());
        }

        // locking
        if (capability.getVersion().isNewerOrEqual(NextcloudVersion.nextcloud_24)) {
            // files_lock app needs to be installed in server for this to work
            assertNotNull(capability.getFilesLockingVersion());
        }

        // groupfolder
        if (capability.getVersion().isNewerOrEqual(NextcloudVersion.nextcloud_27)) {
            if (userId.equals("test")) {
                assertTrue(capability.getGroupfolders().isTrue());
            } else {
                assertTrue(capability.getGroupfolders().isFalse());
            }
        } else {
            assertTrue(capability.getGroupfolders().isFalse());
        }

        // e2e
        assertNotSame(capability.getEndToEndEncryptionApiVersion(), E2EVersion.UNKNOWN);
    }
}
