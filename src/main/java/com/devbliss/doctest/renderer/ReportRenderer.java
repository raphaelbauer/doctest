package com.devbliss.doctest.renderer;

import java.util.List;

import com.devbliss.doctest.items.DocItem;

/**
 * Renders a report in a special format.
 * 
 * @author bmary
 * 
 */
public interface ReportRenderer {

    void render(List<DocItem> listTemplates, String string, String introduction) throws Exception;

}
