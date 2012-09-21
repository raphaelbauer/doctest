package com.devbliss.doctest.machine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.devbliss.doctest.items.AssertDocItem;
import com.devbliss.doctest.items.DocItem;
import com.devbliss.doctest.items.JsonDocItem;
import com.devbliss.doctest.items.RequestDocItem;
import com.devbliss.doctest.items.RequestUploadDocItem;
import com.devbliss.doctest.items.ResponseDocItem;
import com.devbliss.doctest.items.SectionDocItem;
import com.devbliss.doctest.items.TextDocItem;
import com.devbliss.doctest.renderer.ReportRenderer;
import com.devbliss.doctest.utils.JSONHelper;
import com.devbliss.doctest.utils.UriHelper;

import de.devbliss.apitester.ApiTest.HTTP_REQUEST;

/**
 * Unit test for {@link DocTestMachineImpl}
 * 
 * @author bmary
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class DocTestMachineImplUnitTest {

    private static final String CLASS_NAME = "className";
    private static final String TEXT = "text";
    private static final int RESPONSE_CODE = 130;
    private static final String JSON_VALID = "{'abc':'a'}";
    private static final String JSON_INVALID = "invalid";

    @Mock
    private ReportRenderer renderer;
    @Mock
    private JSONHelper jsonHelper;
    @Mock
    private List<DocItem> listItem;
    @Mock
    private UriHelper uriHelper;

    @Captor
    private ArgumentCaptor<List<DocItem>> listItemCaptor;

    private DocTestMachineImpl machine;
    private final HTTP_REQUEST httpRequest = HTTP_REQUEST.GET;
    private String uriWithoutHost;
    private String uriString;
    private URI uri;

    @Before
    public void setUp() throws URISyntaxException {
        uri = new URI("");

        when(jsonHelper.isJsonValid(JSON_VALID)).thenReturn(true);
        when(jsonHelper.prettyPrintJson(JSON_VALID)).thenReturn(JSON_VALID);
        when(uriHelper.uriToString(uri)).thenReturn(uriString);
        machine = spy(new DocTestMachineImpl(renderer, jsonHelper, uriHelper));
        machine.beginDoctest(CLASS_NAME);
    }

    @Test
    public void render() throws Exception {
        when(machine.getListItem()).thenReturn(listItem);
        machine.endDocTest();
        machine.prepareDocTest();
        verify(listItem).clear();
    }

    @Test
    public void addTextItem() throws Exception {
        machine.say(TEXT);
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));

        List<DocItem> listItems = listItemCaptor.getValue();
        assertEquals(1, listItems.size());
        assertTrue(listItems.get(0) instanceof TextDocItem);
        assertEquals(TEXT, ((TextDocItem) listItems.get(0)).getText());
    }

    @Test
    public void addSectionItem() throws Exception {
        machine.sayNextSectionTitle(TEXT);
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));

        List<DocItem> listItems = listItemCaptor.getValue();
        assertEquals(1, listItems.size());
        assertTrue(listItems.get(0) instanceof SectionDocItem);
        assertEquals(TEXT, ((SectionDocItem) listItems.get(0)).getTitle());
    }

    @Test
    public void addAssertItem() throws Exception {
        machine.sayVerify(TEXT);
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));

        List<DocItem> listItems = listItemCaptor.getValue();
        assertEquals(1, listItems.size());
        assertTrue(listItems.get(0) instanceof AssertDocItem);
        assertEquals(TEXT, ((AssertDocItem) listItems.get(0)).getExpected());
    }

    @Test
    public void addResponseItem() throws Exception {
        machine.sayResponse(RESPONSE_CODE, JSON_VALID);
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));

        List<DocItem> listItems = listItemCaptor.getValue();
        assertEquals(1, listItems.size());
        assertTrue(listItems.get(0) instanceof ResponseDocItem);
        assertEquals(JSON_VALID, ((ResponseDocItem) listItems.get(0)).getPayload().getExpected());
        assertEquals(RESPONSE_CODE, ((ResponseDocItem) listItems.get(0)).getResponseCode());
    }

    @Test
    public void addRequestItem() throws Exception {
        machine.sayRequest(uri, JSON_VALID, httpRequest);
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));

        List<DocItem> listItems = listItemCaptor.getValue();
        assertEquals(1, listItems.size());
        assertTrue(listItems.get(0) instanceof RequestDocItem);
        assertEquals(JSON_VALID, ((RequestDocItem) listItems.get(0)).getPayload().getExpected());
        assertEquals(httpRequest, ((RequestDocItem) listItems.get(0)).getHttp());
        assertEquals(uriWithoutHost, ((RequestDocItem) listItems.get(0)).getUri());
    }

    @Test
    public void addUploadRequestItem() throws Exception {
        machine.sayUploadRequest(uri, httpRequest, "file", "fileBody", 10l);
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));

        List<DocItem> listItems = listItemCaptor.getValue();
        assertEquals(1, listItems.size());
        assertTrue(listItems.get(0) instanceof RequestUploadDocItem);
        assertEquals(httpRequest, ((RequestUploadDocItem) listItems.get(0)).getHttp());
        assertEquals(uriWithoutHost, ((RequestUploadDocItem) listItems.get(0)).getUri());
        assertEquals("file", ((RequestUploadDocItem) listItems.get(0)).getFileName());
        assertEquals("fileBody", ((RequestUploadDocItem) listItems.get(0)).getFileBody());
    }

    @Test
    public void addUploadRequestItemUriIsNull() throws Exception {
        machine.sayUploadRequest(null, httpRequest, "file", "fileBody", 10l);
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));

        List<DocItem> listItems = listItemCaptor.getValue();
        assertTrue(listItems.isEmpty());
    }

    @Test
    public void addRequestItemWihtoutUri() throws Exception {
        machine.sayRequest(null, JSON_VALID, httpRequest);
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));

        List<DocItem> listItems = listItemCaptor.getValue();
        assertTrue(listItems.isEmpty());
    }

    @Test
    public void addRequestItemWihtInvalidJson() throws Exception {
        machine.sayRequest(uri, JSON_INVALID, httpRequest);
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));

        List<DocItem> listItems = listItemCaptor.getValue();
        assertEquals(1, listItems.size());
        assertTrue(listItems.get(0) instanceof RequestDocItem);
        assertEquals(JSON_INVALID, ((RequestDocItem) listItems.get(0)).getPayload().getExpected());
        assertEquals(httpRequest, ((RequestDocItem) listItems.get(0)).getHttp());
        assertEquals(uriWithoutHost, ((RequestDocItem) listItems.get(0)).getUri());
    }

    @Test
    public void addJsonDocItem() throws Exception {
        machine.sayPreformatted(JSON_VALID);
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));

        List<DocItem> listItems = listItemCaptor.getValue();
        assertEquals(1, listItems.size());
        assertTrue(listItems.get(0) instanceof JsonDocItem);
        assertEquals(JSON_VALID, ((JsonDocItem) listItems.get(0)).getExpected());
    }

    @Test
    public void testAddSeveralItems() throws Exception {
        machine.say(TEXT + "_first");
        machine.sayPreformatted(JSON_VALID);
        machine.say(TEXT + "_second");
        machine.sayRequest(uri, JSON_VALID, httpRequest);
        machine.sayResponse(RESPONSE_CODE, JSON_VALID);
        machine.sayVerify(TEXT);
        machine.sayNextSectionTitle(TEXT);
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));
        List<DocItem> items = listItemCaptor.getValue();
        assertEquals(7, items.size());
        assertTrue(items.get(0) instanceof TextDocItem);
        assertEquals(TEXT + "_first", ((TextDocItem) items.get(0)).getText());
        assertTrue(items.get(1) instanceof JsonDocItem);
        assertTrue(items.get(2) instanceof TextDocItem);
        assertEquals(TEXT + "_second", ((TextDocItem) items.get(2)).getText());
        assertTrue(items.get(3) instanceof RequestDocItem);
        assertTrue(items.get(4) instanceof ResponseDocItem);
        assertTrue(items.get(5) instanceof AssertDocItem);
        assertTrue(items.get(6) instanceof SectionDocItem);
    }

    @Test
    public void testNameOfTheReport() throws Exception {
        machine.beginDoctest(CLASS_NAME);
        machine.beginDoctest("blabla");
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));
    }

}
