package org.jenkinsci.plugins.prometheus.config;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.YesNoMaybe;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Extension(dynamicLoadable = YesNoMaybe.NO)
public class PrometheusConfiguration extends GlobalConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusConfiguration.class);

    private static final String PROMETHEUS_ENDPOINT = "PROMETHEUS_ENDPOINT";
    private static final String DEFAULT_ENDPOINT = "prometheus";
    static final String COLLECTING_METRICS_PERIOD_IN_SECONDS = "COLLECTING_METRICS_PERIOD_IN_SECONDS";
    static final long DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS = TimeUnit.MINUTES.toSeconds(2);

    private String urlName = null;
    private String additionalPath;
    private String defaultNamespace = "default";
    private boolean useAuthenticatedEndpoint;
    private Long collectingMetricsPeriodInSeconds = null;

    public PrometheusConfiguration() {
        load();
        setPath(urlName);
        setCollectingMetricsPeriodInSeconds(collectingMetricsPeriodInSeconds);
    }

    public static PrometheusConfiguration get() {
        Descriptor configuration = Jenkins.getInstance().getDescriptor(PrometheusConfiguration.class);
        return (PrometheusConfiguration) configuration;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        setPath(json.getString("path"));
        useAuthenticatedEndpoint = json.getBoolean("useAuthenticatedEndpoint");
        defaultNamespace = json.getString("defaultNamespace");
        collectingMetricsPeriodInSeconds = validateProcessingMetricsPeriodInSeconds(json);


        save();
        return super.configure(req, json);
    }

    public String getPath() {
        return StringUtils.isEmpty(additionalPath) ? urlName : urlName + "/" + additionalPath;
    }

    public void setPath(String path) {
        if (path == null) {
            Map<String, String> env = System.getenv();
            path = env.getOrDefault(PROMETHEUS_ENDPOINT, DEFAULT_ENDPOINT);
        }
        urlName = path.split("/")[0];
        List<String> pathParts = Arrays.asList(path.split("/"));
        additionalPath = (pathParts.size() > 1 ? "/" : "") + StringUtils.join(pathParts.subList(1, pathParts.size()), "/");
        save();
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    public void setDefaultNamespace(String path) {
        this.defaultNamespace = path;
        save();
    }

    public long getCollectingMetricsPeriodInSeconds() {
        return collectingMetricsPeriodInSeconds;
    }

    public void setCollectingMetricsPeriodInSeconds(Long collectingMetricsPeriodInSeconds) {
        if (collectingMetricsPeriodInSeconds == null) {
            this.collectingMetricsPeriodInSeconds = parseLongFromEnv();
        } else {
            this.collectingMetricsPeriodInSeconds = collectingMetricsPeriodInSeconds;
        }
        save();
    }

    public boolean isUseAuthenticatedEndpoint() {
        return useAuthenticatedEndpoint;
    }

    public void setUseAuthenticatedEndpoint(boolean useAuthenticatedEndpoint) {
        this.useAuthenticatedEndpoint = useAuthenticatedEndpoint;
        save();
    }

    public String getUrlName() {
        return urlName;
    }

    public String getAdditionalPath() {
        return additionalPath;
    }

    public FormValidation doCheckPath(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.error(Messages.path_required());
        } else if (System.getenv().containsKey(PROMETHEUS_ENDPOINT)) {
            return FormValidation.warning(Messages.path_environment_override(PROMETHEUS_ENDPOINT, System.getenv(PROMETHEUS_ENDPOINT)));
        } else {
            return FormValidation.ok();
        }
    }

    private Long validateProcessingMetricsPeriodInSeconds(JSONObject json) throws FormException {
        try {
            long value = json.getLong("collectingMetricsPeriodInSeconds");
            if (value > 0) {
                return value;
            }
        } catch (JSONException ignored) {
        }
        throw new FormException("CollectingMetricsPeriodInSeconds must be a positive integer", "collectingMetricsPeriodInSeconds");
    }

    private long parseLongFromEnv() {
        Map<String, String> env = System.getenv();
        String message = String.format("COLLECTING_METRICS_PERIOD_IN_SECONDS must be a positive integer. The default value: '%d' will be used instead of provided.", DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS);
        try {
            return Optional.ofNullable(env.get(COLLECTING_METRICS_PERIOD_IN_SECONDS))
                    .map(Long::parseLong)
                    .filter(v -> v > 0)
                    .orElseGet(() -> {
                        logger.warn(message);
                        return DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS;
                    });
        } catch (NumberFormatException e) {
            logger.warn(message);
            return DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS;
        }
    }

}
