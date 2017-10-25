package gherkin;

import gherkin.ast.Location;
import gherkin.deps.com.google.gson.Gson;
import gherkin.exceptions.DialectProviderException;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;

@SuppressWarnings("unchecked")
public class GherkinDialectProvider implements IGherkinDialectProvider {
    private static Map<String, Map<String, List<String>>> dialects;
    private final String defaultDialectName;

    static {
        Gson gson = new Gson();
        try {
            Reader dialectsSource = new InputStreamReader(GherkinDialectProvider.class.getResourceAsStream("/gherkin/gherkin-languages.json"), "UTF-8");
            dialects = gson.fromJson(dialectsSource, Map.class);
        } catch (UnsupportedEncodingException e) {
            throw new DialectProviderException(e);
        }
    }

    public GherkinDialectProvider(String defaultDialectName) {
        this.defaultDialectName = defaultDialectName;
    }

    public GherkinDialectProvider() {
        this("en");
    }

    public GherkinDialect getDefaultDialect() {
        return getDialect(defaultDialectName, null);
    }

    @Override
    public GherkinDialect getDialect(String language, Location location) {
        Map<String, List<String>> map = dialects.get(language);
        if (map == null) {
            throw new ParserException.NoSuchLanguageException(language, location);
        }

        return new GherkinDialect(language, map);
    }

    @Override
    public List<String> getLanguages() {
        List<String> languages = new ArrayList<>(dialects.keySet());
        sort(languages);
        return unmodifiableList(languages);
    }
}
