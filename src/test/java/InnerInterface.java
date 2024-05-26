import java.util.Arrays;

public interface InnerInterface {

    default int stringInterfaceHash(String... s) {
        return Arrays.hashCode(s);
    }

}
