package by.bsuir.ksis.kursovoi.data;

import by.bsuir.ksis.kursovoi.protocol.Peer;

import java.util.Arrays;
import java.util.List;

public class TrackerResponse {

    /** Failure reason. If present, contains human-readable
     * error message as to why request failed.
     */
    private final String failure;

    /**
     * Similar as failure reason, but process can be normally
     * processed (optional).
     */
    private final String warningMessage;

    /** Interval in seconds than client should wait between
     * sending regular requests to tracker.
     */
    private final long interval;

    /** Minimum announce interval. If present
     * clients must not reannounce more frequently than this.
     * (Optional)
     */
    private final long minInterval;

    /** A string, that client should sebd back on it's next
     * on its next announcements. If null, use previous value.
     * (Optional)
     */
    private final String trackerId;

    /**
     * Number of peer with entire file, i.e. seeders.
     */
    private final long complete;

    /**
     * Number if non-seeder peers, aka "leechers"
     */
    private final long incomplete;

    /** The pairs ipv4-port that represents peers. */
    public final List<Peer> peers;


    public TrackerResponse(String failure, String warningMessage, long interval, long minInterval, String trackerId, long complete, long incomplete, List<Peer> peers) {
        this.failure = failure;
        this.warningMessage = warningMessage;
        this.interval = interval;
        this.minInterval = minInterval;
        this.trackerId = trackerId;
        this.complete = complete;
        this.incomplete = incomplete;
        this.peers = peers;
    }

    public static TrackerResponse failure(String failure) {
        return new TrackerResponse(failure, null, 0, 0, null, 0, 0, null);
    }

    public String getFailure() {
        return failure;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public long getInterval() {
        return interval;
    }

    public long getMinInterval() {
        return minInterval;
    }

    public String getTrackerId() {
        return trackerId;
    }

    public long getComplete() {
        return complete;
    }

    public long getIncomplete() {
        return incomplete;
    }

    public List<Peer> getPeers() {
        return peers;
    }

    @Override
    public String toString() {
        return "TrackerResponse{" +
                "failure='" + failure + '\'' +
                "\n, warningMessage='" + warningMessage + '\'' +
                "\n, interval=" + interval +
                "\n, minInterval=" + minInterval +
                "\n, trackerId='" + trackerId + '\'' +
                "\n, complete=" + complete +
                "\n, incomplete=" + incomplete +
                "\n, peers=" + Arrays.toString(peers.toArray()) +
                '}';
    }
}
