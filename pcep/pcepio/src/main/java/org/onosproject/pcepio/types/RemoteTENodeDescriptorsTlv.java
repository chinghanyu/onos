/*
 * Copyright 2015 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.pcepio.types;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.pcepio.exceptions.PcepParseException;
import org.onosproject.pcepio.protocol.PcepVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

/**
 * Provides Remote TE Node Descriptors TLV.
 */
public class RemoteTENodeDescriptorsTlv implements PcepValueType {

    /* Reference :PCEP Extension for Transporting TE Data
        draft-dhodylee-pce-pcep-te-data-extn-02
     *
          0                   1                   2                   3
          0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
         +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         |           Type=[TBD9]         |             Length            |
         +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         |                                                               |
         //              Node Descriptor Sub-TLVs (variable)            //
         |                                                               |
         +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     */

    protected static final Logger log = LoggerFactory.getLogger(RemoteTENodeDescriptorsTlv.class);

    public static final short TYPE = 1003; //TODD:change this TBD9
    public short hLength;

    public static final int TLV_HEADER_LENGTH = 4;
    // Node Descriptor Sub-TLVs (variable)
    private LinkedList<PcepValueType> llRemoteTENodeDescriptorSubTLVs;

    /**
     * Constructor to initialize llRemoteTENodeDescriptorSubTLVs.
     *
     * @param llRemoteTENodeDescriptorSubTLVs LinkedList of PcepValueType
     */
    public RemoteTENodeDescriptorsTlv(LinkedList<PcepValueType> llRemoteTENodeDescriptorSubTLVs) {
        this.llRemoteTENodeDescriptorSubTLVs = llRemoteTENodeDescriptorSubTLVs;
    }

    /**
     * Returns object of Remote TE Node Descriptors TLV.
     *
     * @param llRemoteTENodeDescriptorSubTLVs LinkedList of PcepValueType
     * @return object of RemoteTENodeDescriptorsTLV
     */
    public static RemoteTENodeDescriptorsTlv of(final LinkedList<PcepValueType> llRemoteTENodeDescriptorSubTLVs) {
        return new RemoteTENodeDescriptorsTlv(llRemoteTENodeDescriptorSubTLVs);
    }

    /**
     * Returns Remote TE Node Descriptor Sub TLVs.
     *
     * @return llRemoteTENodeDescriptorSubTLVs
     */
    public LinkedList<PcepValueType> getllRemoteTENodeDescriptorSubTLVs() {
        return llRemoteTENodeDescriptorSubTLVs;
    }

    @Override
    public PcepVersion getVersion() {
        return PcepVersion.PCEP_1;
    }

    @Override
    public short getType() {
        return TYPE;
    }

    @Override
    public short getLength() {
        return hLength;
    }

    @Override
    public int hashCode() {
        return Objects.hash(llRemoteTENodeDescriptorSubTLVs.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        /*
         * Here we have a list of Tlv so to compare each sub tlv between the object
         * we have to take a list iterator so one by one we can get each sub tlv object
         * and can compare them.
         * it may be possible that the size of 2 lists is not equal so we have to first check
         * the size, if both are same then we should check for the subtlv objects otherwise
         * we should return false.
         */
        if (obj instanceof RemoteTENodeDescriptorsTlv) {
            int countObjSubTlv = 0;
            int countOtherSubTlv = 0;
            boolean isCommonSubTlv = true;
            RemoteTENodeDescriptorsTlv other = (RemoteTENodeDescriptorsTlv) obj;
            Iterator<PcepValueType> objListIterator = ((RemoteTENodeDescriptorsTlv) obj).llRemoteTENodeDescriptorSubTLVs
                    .iterator();
            countObjSubTlv = ((RemoteTENodeDescriptorsTlv) obj).llRemoteTENodeDescriptorSubTLVs.size();
            countOtherSubTlv = other.llRemoteTENodeDescriptorSubTLVs.size();
            if (countObjSubTlv != countOtherSubTlv) {
                return false;
            } else {
                while (objListIterator.hasNext() && isCommonSubTlv) {
                    PcepValueType subTlv = objListIterator.next();
                    isCommonSubTlv = Objects.equals(llRemoteTENodeDescriptorSubTLVs.contains(subTlv),
                            other.llRemoteTENodeDescriptorSubTLVs.contains(subTlv));
                }
                return isCommonSubTlv;
            }
        }
        return false;
    }

    @Override
    public int write(ChannelBuffer c) {

        int tlvStartIndex = c.writerIndex();
        c.writeShort(TYPE);
        int tlvLenIndex = c.writerIndex();
        hLength = 0;
        c.writeShort(hLength);

        ListIterator<PcepValueType> listIterator = llRemoteTENodeDescriptorSubTLVs.listIterator();

        while (listIterator.hasNext()) {
            PcepValueType tlv = listIterator.next();

            if (tlv == null) {
                log.debug("TLV is null from subTlv list");
                continue;
            }
            tlv.write(c);

            // need to take care of padding
            int pad = tlv.getLength() % 4;

            if (0 != pad) {
                pad = 4 - pad;
                for (int i = 0; i < pad; ++i) {
                    c.writeByte((byte) 0);
                }
            }
        }

        hLength = (short) (c.writerIndex() - tlvStartIndex);
        c.setShort(tlvLenIndex, (hLength - TLV_HEADER_LENGTH));

        return c.writerIndex() - tlvStartIndex;
    }

    /**
     * Reads channel buffer and returns object of Remote TE Node Descriptors TLV.
     *
     * @param c input channel buffer
     * @return object of RemoteTENodeDescriptorsTLV
     * @throws PcepParseException if mandatory fields are missing
     */
    public static PcepValueType read(ChannelBuffer c , short length) throws PcepParseException {

        // Node Descriptor Sub-TLVs (variable)
        LinkedList<PcepValueType> llRemoteTENodeDescriptorSubTLVs = new LinkedList<PcepValueType>();

        ChannelBuffer tempCb = c.readBytes(length);

        while (TLV_HEADER_LENGTH <= tempCb.readableBytes()) {

            PcepValueType tlv;
            short hType = tempCb.readShort();
            int iValue = 0;
            short hLength = tempCb.readShort();
            switch (hType) {

            case AutonomousSystemTlv.TYPE:
                iValue = tempCb.readInt();
                tlv = new AutonomousSystemTlv(iValue);
                break;
            case BGPLSidentifierTlv.TYPE:
                iValue = tempCb.readInt();
                tlv = new BGPLSidentifierTlv(iValue);
                break;
            case OSPFareaIDsubTlv.TYPE:
                iValue = tempCb.readInt();
                tlv = new OSPFareaIDsubTlv(iValue);
                break;
            case RouterIDSubTlv.TYPE:
                tlv = RouterIDSubTlv.read(tempCb, hLength);
                break;

            default:
                throw new PcepParseException("Unsupported Sub TLV type :" + hType);
            }

            // Check for the padding
            int pad = hLength % 4;
            if (0 < pad) {
                pad = 4 - pad;
                if (pad <= tempCb.readableBytes()) {
                    tempCb.skipBytes(pad);
                }
            }

            llRemoteTENodeDescriptorSubTLVs.add(tlv);
        }

        if (0 < tempCb.readableBytes()) {

            throw new PcepParseException("Sub Tlv parsing error. Extra bytes received.");
        }
        return new RemoteTENodeDescriptorsTlv(llRemoteTENodeDescriptorSubTLVs);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("Type", TYPE)
                .add("Length", hLength)
                .add("RemoteTeNodeDescriptorSubTLVs", llRemoteTENodeDescriptorSubTLVs)
                .toString();
    }
}
