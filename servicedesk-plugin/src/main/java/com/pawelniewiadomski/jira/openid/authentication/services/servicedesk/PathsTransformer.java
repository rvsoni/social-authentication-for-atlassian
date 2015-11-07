package com.pawelniewiadomski.jira.openid.authentication.services.servicedesk;

import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.CharSequenceDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import com.pawelniewiadomski.jira.openid.authentication.PluginKey;
import com.pawelniewiadomski.jira.openid.authentication.ServiceDeskPluginKey;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathsTransformer implements WebResourceTransformer {

    @Autowired
    PluginKey pluginKey;

    @Override
    public DownloadableResource transform(Element element, final ResourceLocation resourceLocation, String s, DownloadableResource downloadableResource) {
        return new CharSequenceDownloadableResource(downloadableResource) {
            @Override
            protected CharSequence transform(CharSequence charSequence) {
                final Matcher matcher = Pattern.compile("(\\/jira-openid-authentication\\/)").matcher(charSequence);
                int start = 0;

                StringBuilder out;
                for(out = null; matcher.find(); start = matcher.end()) {
                    if(out == null) {
                        out = new StringBuilder();
                    }

                    out.append(charSequence.subSequence(start, matcher.start()));
                    String subst = String.format("/%s/", ((ServiceDeskPluginKey) pluginKey).getRestKey());
                    if(subst != null) {
                        out.append(subst);
                    } else {
                        out.append(matcher.group());
                    }
                }

                if(out == null) {
                    return charSequence;
                } else {
                    out.append(charSequence.subSequence(start, charSequence.length()));
                    return out;
                }
            }
        };
    }
}
