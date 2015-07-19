package com.ndnhealthnet.androidudpclient.DB.DBDataTypes;

/**
 * Container for a Packet entry (that is to be stored locally)
 */
public class PacketDBEntry {

    private String packetName;
    private String packetContent;

    /**
     * Constructor for object that holds packet data that
     * is to be stored/manipulated by the database
     *
     * @param packetName of given packet
     * @param packetContent of given packet
     */
    public PacketDBEntry(String packetName, String packetContent) {
        this.packetName = packetName;
        this.packetContent = packetContent;
    }

    public PacketDBEntry() {}

    public String getPacketName() {
        return packetName;
    }

    public void setPacketName(String packetName) {
        this.packetName = packetName;
    }

    public String getPacketContent() {
        return packetContent;
    }

    public void setPacketContent(String packetContent) {
        this.packetContent = packetContent;
    }
}
