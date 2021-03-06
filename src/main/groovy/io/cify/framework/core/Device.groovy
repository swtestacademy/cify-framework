package io.cify.framework.core

import io.cify.framework.core.interfaces.IDevice
import io.cify.framework.logging.LoggingOutputStream
import io.cify.framework.recording.RecordingController
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.MarkerManager
import org.apache.logging.log4j.core.Logger
import org.openqa.selenium.WebDriver
import org.openqa.selenium.remote.DesiredCapabilities

/**
 * Created by FOB Solutions
 *
 * This is a model class for Device
 */

class Device implements IDevice {

    private static final Logger LOG = LogManager.getLogger(this.class) as Logger
    private static final Marker MARKER = MarkerManager.getMarker('DEVICE') as Marker

    private String id
    private DeviceCategory category
    private DesiredCapabilities capabilities

    private WebDriver driver

    public boolean isRecording = false
    public boolean active = false

    /**
     * Default constructor for Device
     *
     * @param id unique device id
     * @param category device category
     * @param capabilities device desired capabilities
     * */
    protected Device(String id, DeviceCategory category, DesiredCapabilities capabilities) {
        LOG.debug(MARKER, "Create new device with id $id, category $category and capabilities $capabilities")
        this.id = id
        this.category = category
        this.capabilities = capabilities
    }

    /**
     * Gets id of device
     *
     * @retun device id
     * */
    @Override
    public String getId() {
        LOG.debug(MARKER, "Get device id")
        return id
    }

    /**
     * Gets category for device
     *
     * @retun device category
     * */
    @Override
    public DeviceCategory getCategory() {
        LOG.debug(MARKER, "Get device category")
        return this.category
    }

    /**
     * Gets driver from Device
     *
     * @return WebDriver
     * */
    @Override
    public WebDriver getDriver() {
        LOG.debug(MARKER, "Get device driver")
        return driver
    }

    /**
     * Sets desired capability parameter
     *
     * @param key
     * @param value
     * */
    @Override
    public void setCapability(String key, String value) {
        LOG.debug(MARKER, "Set desire capability $key : $value")
        if (key != null && value != null) {
            capabilities.setCapability(key, value)
        }
    }

    /**
     * Gets desired capabilities for device
     *
     * @retun DesiredCapabilities
     * */
    @Override
    public DesiredCapabilities getCapabilities() {
        LOG.debug(MARKER, "Get all desired capabilities")
        return capabilities
    }

    /**
     * Opens app on device
     * */
    @Override
    public void openApp() {
        openApp("", "", "")
    }

    /**
     * Opens app on device
     *
     * @param app
     * */
    @Override
    public void openApp(String app) {
        openApp(app, "", "");
    }

    /**
     * Opens app on device
     *
     * @param app
     * @param appActivity
     * @param appPackage
     *
     * @throws CifyFrameworkException  if failed to open app
     * */
    @Override
    public void openApp(String app, String appActivity, String appPackage) {
        LOG.debug(MARKER, "Open app $app, $appActivity, $appPackage")
        try {
            File appFile = new File(app)
            if (appFile.isFile() && (!app.startsWith("http://") && !app.startsWith("https://"))) {
                String fileName = appFile.getName()
                String path = appFile.toURI().getRawPath().replace(fileName, "").replace(":", "").toLowerCase()
                app = path + fileName
            }

            app ? setCapability("app", app) : null
            appActivity ? setCapability("appActivity", appActivity) : null
            appPackage ? setCapability("appPackage", appPackage) : null
            createDriver()

        } catch (all) {
            LOG.debug(MARKER, all.message, all)
            throw new CifyFrameworkException("Failed to open application cause $all.message")
        }
    }

    /**
     * Opens url in device browser
     *
     * @param url
     *
     * @throws CifyFrameworkException  if failed to open url
     * */
    @Override
    public void openBrowser(String url) {
        LOG.debug(MARKER, "Open url $url")
        try {
            if (!validateUrl(url)) {
                throw new CifyFrameworkException("Url is not valid")
            }
            createDriver()
            getDriver().get(url)

        } catch (all) {
            LOG.debug(MARKER, all.message, all)
            throw new CifyFrameworkException("Failed to open browser cause $all.message")
        }
    }

    /**
     * Starts video recording
     * */
    @Override
    void startRecording() {
        RecordingController.startRecording(this)
    }

    /**
     * Stops video recording
     * */
    @Override
    void stopRecording() {
        isRecording = false
        RecordingController.stopRecording(this)
    }

    /**
     * Quits app or browser
     * */
    @Override
    void quit() {
        LOG.debug(MARKER, "Starting to quit device with category $category")
        try {
            if (isRecording) {
                RecordingController.takeScreenshot(this)
                stopRecording()
            }

            if (hasDriver()) {
                LOG.debug(MARKER, "Quit device driver")
                getDriver().quit()
            }
        } catch (all) {
            LOG.error(MARKER, "Failed to quit device cause $all.message")
        }
    }

    /**
     * Checks if driver exists
     * */
    boolean hasDriver() {
        LOG.debug(MARKER, "Check if driver exists")
        if (getDriver() == null) {
            LOG.debug(MARKER, "No driver found")
            return false
        }

        LOG.debug(MARKER, "Driver found")
        return true
    }

    /**
     * Creates webdriver for device
     * */
    private void createDriver() {
        LOG.debug(MARKER, "Create new device driver")
        LoggingOutputStream.redirectSystemOutAndSystemErrToLogger()
        quit()
        DeviceManager.getInstance().setDeviceActive(this)
        WebDriver driver = DriverFactory.getDriver(getCapabilities())
        this.driver = driver

        if (System.getProperty("videoRecord") == "true") {
            startRecording()
        }
    }

    /**
     * Validates url
     *
     * @param url
     *
     * @return boolean
     * */
    private static boolean validateUrl(String url) {
        !(url == null || url.isEmpty())
    }
}