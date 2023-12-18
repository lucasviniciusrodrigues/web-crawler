package com.axreng.backend.controller;

import com.axreng.backend.exception.NotFoundException;
import com.axreng.backend.service.CrawlerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import spark.Request;
import spark.Response;

import static com.axreng.backend.Mocks.getCrawlerBaseEntity;
import static com.axreng.backend.Mocks.getCrawlerDetailModel;
import static com.axreng.backend.Mocks.validKeyowrd;
import static com.axreng.backend.utils.Utils.gson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CrawlerControllerTest {

    @Mock
    private CrawlerService crawlerService;

    @Mock
    private Request request;

    @Mock
    private Response response;

    private CrawlerController crawlerController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        crawlerController = new CrawlerController(crawlerService);
    }

    @Test
    public void shouldReturnValidResponseFromPost() throws IllegalAccessException {
        when(request.contentType()).thenReturn("application/json");
        when(request.body()).thenReturn("{\"keyword\":\"test\"}");
        when(crawlerService.getValidKeyword(any())).thenReturn(validKeyowrd);
        when(crawlerService.post(anyString())).thenReturn(getCrawlerBaseEntity());

        String result = crawlerController.postCrawler(request, response);

        assertEquals(gson.toJson(getCrawlerBaseEntity()), result);
        verify(crawlerService, times(1)).post(anyString());
    }

    @Test
    public void shouldReturnValidResponseFromGet() throws NotFoundException {
        when(request.params("id")).thenReturn(validKeyowrd);
        when(crawlerService.get(anyString())).thenReturn(
                gson.toJson(getCrawlerDetailModel())
        );

        String result = crawlerController.getById(request, response);

        assertEquals(gson.toJson(getCrawlerDetailModel()), result);
        verify(crawlerService, times(1)).get(anyString());
    }

    @Test
    public void shouldReturn404() throws NotFoundException {
        when(request.params("id")).thenReturn(validKeyowrd);
        when(crawlerService.get(anyString())).thenThrow(new NotFoundException("Not found"));

        String result = crawlerController.getById(request, response);

        assertEquals("{\"status\":404,\"message\":\"Not found\"}", result);
        verify(crawlerService, times(1)).get(anyString());
    }

    @Test
    public void shouldReturn400IdTooSmall(){
        when(request.params("id")).thenReturn("123");

        String result = crawlerController.getById(request, response);

        assertEquals("{\"status\":400,\"message\":\"" + crawlerController.badRequestIdMessage + "\"}", result);
    }

    @Test
    public void shouldReturn400IdTooBig() {
        when(request.params("id")).thenReturn("123123123012312312301231231230123123");

        String result = crawlerController.getById(request, response);

        assertEquals("{\"status\":400,\"message\":\"" + crawlerController.badRequestIdMessage + "\"}", result);
    }

    @Test
    public void shouldReturn400InvalidContentType(){
        crawlerController.postCrawler()
    }
}