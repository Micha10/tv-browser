package captureplugin.drivers.simpledevice;

import captureplugin.drivers.utils.ProgramTime;
import devplugin.Channel;
import devplugin.Program;

/**
 * Small interface for applescript devices
 */
public interface SimpleConnectionIf {

    /**
     * Get all available Channels of this device
     * @return available channels
     */
    SimpleChannel[] getAvailableChannels();

    /**
     * Get all recordings of this device
     * @param conf configuration
     * @return all recordings
     */
    Program[] getAllRecordings(SimpleConfig conf);

    /**
     * Add a recording
     *
     * @param conf configuration of this device
     * @param prg add this Program
     * @return <code>true</code> if successfully
     */
    boolean addToRecording(SimpleConfig conf, ProgramTime prg, boolean onlyGuiAtErrors);

    /**
     * Remove a recording
     *
     * @param conf Configuration
     * @param prg Program to remove
     */
    void removeRecording(SimpleConfig conf, Program prg, boolean onlyGuiAtErrors);

    /**
     * Switch to channel
     * @param conf Configuration of this device
     * @param channel Channel
     */
    void switchToChannel(SimpleConfig conf, Channel channel);
}
