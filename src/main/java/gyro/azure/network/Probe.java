package gyro.azure.network;

import com.microsoft.azure.management.network.ApplicationGateway.Update;
import com.microsoft.azure.management.network.ApplicationGatewayProbe;
import com.microsoft.azure.management.network.ApplicationGatewayProbe.UpdateDefinitionStages.WithProtocol;
import com.microsoft.azure.management.network.ApplicationGatewayProbe.UpdateDefinitionStages.WithAttach;
import com.microsoft.azure.management.network.ApplicationGatewayProtocol;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.diff.Diffable;
import gyro.core.diff.ResourceDiffProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Probe extends Diffable {
    private String probeName;
    private String hostName;
    private String path;
    private Integer interval;
    private Integer timeout;
    private Integer unhealthyThreshold;
    private Boolean httpsProtocol;
    private List<String> httpResponseCodes;
    private String httpResponseBodyMatch;

    public Probe() {

    }

    public Probe(ApplicationGatewayProbe probe) {
        setProbeName(probe.name());
        setHostName(probe.host());
        setPath(probe.path());
        setInterval(probe.timeBetweenProbesInSeconds());
        setTimeout(probe.timeoutInSeconds());
        setUnhealthyThreshold(probe.retriesBeforeUnhealthy());
        setHttpResponseCodes(new ArrayList<>(probe.healthyHttpResponseStatusCodeRanges()));
        setHttpResponseBodyMatch(probe.healthyHttpResponseBodyContents());
        setHttpsProtocol(probe.inner().protocol().equals(ApplicationGatewayProtocol.HTTPS));
    }

    public String getProbeName() {
        return probeName;
    }

    public void setProbeName(String probeName) {
        this.probeName = probeName;
    }

    @ResourceDiffProperty(updatable = true)
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @ResourceDiffProperty(updatable = true)
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getInterval() {
        if (interval == null) {
            interval = 30;
        }

        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getTimeout() {
        if (timeout == null) {
            timeout = 30;
        }

        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getUnhealthyThreshold() {
        if (unhealthyThreshold == null) {
            unhealthyThreshold = 3;
        }

        return unhealthyThreshold;
    }

    public void setUnhealthyThreshold(Integer unhealthyThreshold) {
        this.unhealthyThreshold = unhealthyThreshold;
    }

    @ResourceDiffProperty(updatable = true)
    public Boolean getHttpsProtocol() {
        if (httpsProtocol == null) {
            httpsProtocol = false;
        }

        return httpsProtocol;
    }

    public void setHttpsProtocol(Boolean httpsProtocol) {
        this.httpsProtocol = httpsProtocol;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getHttpResponseCodes() {
        if (httpResponseCodes == null) {
            httpResponseCodes = new ArrayList<>();
        }

        return httpResponseCodes;
    }

    public void setHttpResponseCodes(List<String> httpResponseCodes) {
        this.httpResponseCodes = httpResponseCodes;
    }

    @ResourceDiffProperty(updatable = true)
    public String getHttpResponseBodyMatch() {
        return httpResponseBodyMatch;
    }

    public void setHttpResponseBodyMatch(String httpResponseBodyMatch) {
        this.httpResponseBodyMatch = httpResponseBodyMatch;
    }

    @Override
    public String primaryKey() {
        return getProbeName();
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("probe");

        if (!ObjectUtils.isBlank(getProbeName())) {
            sb.append(" - ").append(getProbeName());
        }

        return sb.toString();
    }

    Update createProbe(Update update) {
        WithProtocol<Update> updateWithProtocol = update.defineProbe(getProbeName())
            .withHost(getHostName()).withPath(getPath());

        WithAttach<Update> updateWithAttach;

        if (getHttpsProtocol()) {
            updateWithAttach = updateWithProtocol.withHttps().withTimeoutInSeconds(getTimeout());
        } else {
            updateWithAttach = updateWithProtocol.withHttp().withTimeoutInSeconds(getTimeout());
        }

        updateWithAttach = updateWithAttach.withRetriesBeforeUnhealthy(getUnhealthyThreshold())
            .withTimeBetweenProbesInSeconds(getInterval());

        if (!getHttpResponseCodes().isEmpty()) {
            update = updateWithAttach.withHealthyHttpResponseStatusCodeRanges(new HashSet<>(getHttpResponseCodes()))
                .withHealthyHttpResponseBodyContents(getHttpResponseBodyMatch())
                .attach();
        } else {
            update = updateWithAttach.attach();
        }

        return update;
    }

    Update updateProbe(Update update) {
        ApplicationGatewayProbe.Update partialUpdate = update.updateProbe(getProbeName())
            .withHost(getHostName()).withPath(getPath());

        if (getHttpsProtocol()) {
            partialUpdate = partialUpdate.withHttps().withTimeoutInSeconds(getTimeout());
        } else {
            partialUpdate = partialUpdate.withHttp().withTimeoutInSeconds(getTimeout());
        }

        partialUpdate = partialUpdate.withRetriesBeforeUnhealthy(getUnhealthyThreshold())
            .withTimeBetweenProbesInSeconds(getInterval());

        if (!getHttpResponseCodes().isEmpty()) {
            update = partialUpdate.withoutHealthyHttpResponseStatusCodeRanges()
                .withHealthyHttpResponseStatusCodeRanges(new HashSet<>(getHttpResponseCodes()))
                .withHealthyHttpResponseBodyContents(getHttpResponseBodyMatch())
                .parent();
        } else {
            update = partialUpdate.parent();
        }

        return update;
    }
}
