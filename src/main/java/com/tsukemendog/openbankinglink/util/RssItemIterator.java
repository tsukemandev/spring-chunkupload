package com.tsukemendog.openbankinglink.util;

import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.DateTime;
import com.apptasticsoftware.rssreader.DateTimeParser;
import com.apptasticsoftware.rssreader.Item;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

class RssItemIterator implements Iterator<Item> {
    private StringBuilder textBuilder;
    private Map<String, StringBuilder> childNodeTextBuilder;
    private InputStream is;
    private Deque<String> elementStack;

    private XMLStreamReader reader;
    private Channel channel;
    private Item item = null;
    private Item nextItem;
    private boolean isChannelPart = false;
    private boolean isItemPart = false;


    private final Map<String, String> headers = new HashMap<>();
    private final HashMap<String, BiConsumer<Channel, String>> channelTags = new HashMap<>();
    private final HashMap<String, Map<String, BiConsumer<Channel, String>>> channelAttributes = new HashMap<>();
    private final HashMap<String, BiConsumer<Item, String>> itemTags = new HashMap<>();
    private final HashMap<String, Map<String, BiConsumer<Item, String>>> itemAttributes = new HashMap<>();

    private DateTimeParser dateTimeParser = new DateTime();


    private final Set<String> collectChildNodesForTag = Set.of("content", "summary");

    @SuppressWarnings("java:S1133")
    @Deprecated(since="3.5.0", forRemoval=true)
    protected Channel createChannel() {
        return null;
    }

    public RssItemIterator(InputStream is) {
        this.is = is;
        nextItem = null;
        textBuilder = new StringBuilder();
        childNodeTextBuilder = new HashMap<>();
        elementStack = new ArrayDeque<>();

        try {
            var xmlInFact = XMLInputFactory.newInstance();

            // disable XML external entity (XXE) processing
            xmlInFact.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            xmlInFact.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            xmlInFact.setProperty(XMLInputFactory.IS_COALESCING, true);

            reader = xmlInFact.createXMLStreamReader(is);

        }
        catch (XMLStreamException e) {
            var logger = Logger.getLogger("log");

            if (logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, "Failed to process XML. ", e);
        }
    }

    public void close() {
        try {
            reader.close();
            is.close();
        } catch (XMLStreamException | IOException e) {
            var logger = Logger.getLogger("log");

            if (logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, "Failed to close XML stream. ", e);
        }
    }

    void peekNext() {
        if (nextItem == null) {
            try {
                nextItem = next();
            }
            catch (NoSuchElementException e) {
                nextItem = null;
            }
        }
    }

    @Override
    public boolean hasNext() {
        peekNext();
        return nextItem != null;
    }

    @Override
    @SuppressWarnings("squid:S3776")
    public Item next() {
        if (nextItem != null) {
            var next = nextItem;
            nextItem = null;

            return next;
        }

        try {
            while (reader.hasNext()) {
                var type = reader.next();
                collectChildNodes(type);

                if (type == CHARACTERS || type == CDATA) {
                    parseCharacters();
                }
                else if (type == START_ELEMENT) {
                    parseStartElement();
                    parseAttributes();
                }
                else if (type == END_ELEMENT) {
                    var itemParsed = parseEndElement();

                    if (itemParsed)
                        return item;
                }
            }
        } catch (XMLStreamException e) {
            var logger = Logger.getLogger("log");

            if (logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, "Failed to parse XML. ", e);
        }

        close();
        throw new NoSuchElementException();
    }


    void collectChildNodes(int type) {
        if (type == START_ELEMENT) {
            var nsTagName = toNsName(reader.getPrefix(), reader.getLocalName());

            if (!childNodeTextBuilder.isEmpty()) {
                StringBuilder startTagBuilder = new StringBuilder("<").append(nsTagName);
                // Add namespaces to start tag
                for (int i = 0; i < reader.getNamespaceCount(); ++i) {
                    startTagBuilder.append(" ")
                            .append(toNamespacePrefix(reader.getNamespacePrefix(i)))
                            .append("=")
                            .append(reader.getNamespaceURI(i));
                }
                // Add attributes to start tag
                for (int i = 0; i < reader.getAttributeCount(); ++i) {
                    startTagBuilder.append(" ")
                            .append(toNsName(reader.getAttributePrefix(i), reader.getAttributeLocalName(i)))
                            .append("=")
                            .append(reader.getAttributeValue(i));
                }
                startTagBuilder.append(">");
                var startTag = startTagBuilder.toString();

                childNodeTextBuilder.entrySet()
                        .stream()
                        .filter(e -> !e.getKey().equals(nsTagName))
                        .forEach(e -> e.getValue().append(startTag));
            }

            // Collect child notes for tag names in this set
            if (collectChildNodesForTag.contains(nsTagName)) {
                childNodeTextBuilder.put(nsTagName, new StringBuilder());
            }
        }
        else if (type == CHARACTERS || type == CDATA) {
            childNodeTextBuilder.forEach((k, builder) -> builder.append(reader.getText()));
        }
        else if (type == END_ELEMENT) {
            var nsTagName = toNsName(reader.getPrefix(), reader.getLocalName());
            var endTag = "</" + nsTagName + ">";
            childNodeTextBuilder.entrySet()
                    .stream()
                    .filter(e -> !e.getKey().equals(nsTagName))
                    .forEach(e -> e.getValue().append(endTag));
        }
    }

    @SuppressWarnings("java:S5738")
    void parseStartElement() {
        textBuilder.setLength(0);
        var nsTagName = toNsName(reader.getPrefix(), reader.getLocalName());
        elementStack.addLast(nsTagName);

        if (isChannel(nsTagName)) {
            channel = Objects.requireNonNullElse(createChannel(dateTimeParser), createChannel());
            channel.setTitle("");
            channel.setDescription("");
            channel.setLink("");
            isChannelPart = true;
        }
        else if (isItem(nsTagName)) {
            item = Objects.requireNonNullElse(createItem(dateTimeParser), createItem());
            item.setChannel(channel);
            isChannelPart = false;
            isItemPart = true;
        }
    }

    @SuppressWarnings("java:S1133")
    @Deprecated(since="3.5.0", forRemoval=true)
    protected Item createItem(DateTimeParser dateTimeParser) {
        return null;
    }

    protected Channel createChannel(DateTimeParser dateTimeParser) {
        return null;
    }


    protected Item createItem() {
        return null;
    }


    protected boolean isChannel(String tagName) {
        return "channel".equals(tagName) || "feed".equals(tagName);
    }

    protected boolean isItem(String tagName) {
        return "item".equals(tagName) || "entry".equals(tagName);
    }


    void parseAttributes() {
        var nsTagName = toNsName(reader.getPrefix(), reader.getLocalName());
        var elementFullPath = getElementFullPath();

        if (isChannelPart) {
            // Map channel attributes
            mapChannelAttributes(nsTagName);
            mapChannelAttributes(elementFullPath);
        }
        else if (isItemPart) {
            // Map item attributes
            mapItemAttributes(nsTagName);
            mapItemAttributes(elementFullPath);
        }
    }

    void mapChannelAttributes(String key) {
        var consumers = channelAttributes.get(key);
        if (consumers != null && channel != null) {
            consumers.forEach((attributeName, consumer) -> {
                var attributeValue = Optional.ofNullable(reader.getAttributeValue(null, attributeName));
                attributeValue.ifPresent(v -> consumer.accept(channel, v));
            });
        }
    }

    void mapItemAttributes(String key) {
        var consumers = itemAttributes.get(key);
        if (consumers != null && item != null) {
            consumers.forEach((attributeName, consumer) -> {
                var attributeValue = Optional.ofNullable(reader.getAttributeValue(null, attributeName));
                attributeValue.ifPresent(v -> consumer.accept(item, v));
            });
        }
    }

    boolean parseEndElement() {
        var nsTagName = toNsName(reader.getPrefix(), reader.getLocalName());
        var text = textBuilder.toString().trim();
        var elementFullPath = getElementFullPath();
        elementStack.removeLast();

        if (isChannelPart)
            parseChannelCharacters(channel, nsTagName, elementFullPath, text);
        else
            parseItemCharacters(item, nsTagName, elementFullPath, text);

        textBuilder.setLength(0);

        return isItem(nsTagName);
    }

    void parseCharacters() {
        var text = reader.getText();

        if (text.isBlank())
            return;

        textBuilder.append(text);
    }

    void parseChannelCharacters(Channel channel, String nsTagName, String elementFullPath, String text) {
        if (channel == null || text.isEmpty())
            return;

        channelTags.computeIfPresent(nsTagName, (k, f) -> { f.accept(channel, text); return f; });
        channelTags.computeIfPresent(elementFullPath, (k, f) -> { f.accept(channel, text); return f; });
    }

    void parseItemCharacters(final Item item, String nsTagName, String elementFullPath, final String text) {
        var builder = childNodeTextBuilder.remove(nsTagName);
        if (item == null || (text.isEmpty() && builder == null))
            return;

        var textValue = (builder != null) ? builder.toString().trim() : text;
        itemTags.computeIfPresent(nsTagName, (k, f) -> { f.accept(item, textValue); return f; });
        itemTags.computeIfPresent(elementFullPath, (k, f) -> { f.accept(item, text); return f; });
    }

    String toNsName(String prefix, String name) {
        return prefix.isEmpty() ? name : prefix + ":" + name;
    }

    String toNamespacePrefix(String prefix) {
        return prefix == null || prefix.isEmpty() ? "xmlns" : "xmlns" + ":" + prefix;
    }

    String getElementFullPath() {
        return "/" + String.join("/", elementStack);
    }

}
