/*
 * ShownHideMacro.java    23 December 2010, 10:54
 *
 * Copyright 2010 Drunken Dev.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.drunkendev.confluence.plugins.shownhide;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.macro.WysiwygBodyType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang.StringUtils;


/**
 * Macro to only render its contents if the current date is within a specified
 * start/end date range.
 *
 * @author  Brett Ryan
 * @version 1.1
 */
public class ShownHideMacro extends BaseMacro implements Macro {

    private static final String KEY_START_DATE = "startDate";
    private static final String KEY_END_DATE = "endDate";
    private final SimpleDateFormat isoFullFormat;
    private final SimpleDateFormat isoDateFormat;
    private final SimpleDateFormat fullFormat;
    private final SimpleDateFormat dateFormat;

    /**
     * Creates a new {@code ShownHideMacro} instance.
     */
    public ShownHideMacro() {
        isoFullFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        fullFormat = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(
                SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
        dateFormat = (SimpleDateFormat) SimpleDateFormat.getDateInstance(
                SimpleDateFormat.SHORT);
    }

    /**
     * I have to return false here in order to stop the leading paragraph from
     * being rendered, I'm not sure why.
     *
     * @return  false - this is not an in-line macro.
     */
    @Override
    public boolean isInline() {
        return false;
    }

    /**
     * Determines if the macro has a body content.
     *
     * @return  true - this macro requires a body.
     */
    @Override
    public boolean hasBody() {
        return true;
    }

    /**
     * Gets the rendering mode for this macro.
     *
     * @return  Everything except {@link com.atlassian.renderer.v2.RenderMode#F_FIRST_PARA}.
     */
    @Override
    public RenderMode getBodyRenderMode() {
        return RenderMode.suppress(RenderMode.F_FIRST_PARA);
    }

    /**
     * Execute the macro.
     *
     * @param   parameters
     *          Macro parameters - We look for fromDate and toDate.
     * @param   body
     *          Content of the body of the macro.
     * @param   renderContext
     *          Rendering context in which the macro was executed.
     * @return  Output of the macro
     * @throws  MacroException
     *          If the macro fails in some unremarkable way. If the macro fails
     *          in a way that is important to the server maintainer (i.e.
     *          something is badly wrong), throw a RuntimeException instead.
     */
    @Override
    public String execute(
            final Map parameters,
            final String body,
            final RenderContext renderContext)
            throws MacroException {
        if (StringUtils.isBlank(body)) {
            return StringUtils.EMPTY;
        }
        long now = new Date().getTime();
        try {
            if (parameters.containsKey(KEY_START_DATE)) {
                if (getDate((String) parameters.get(KEY_START_DATE)).getTime() >= now) {
                    return StringUtils.EMPTY;
                }
            }
            if (parameters.containsKey(KEY_END_DATE)) {
                if (getDate((String) parameters.get(KEY_END_DATE)).getTime() < now) {
                    return StringUtils.EMPTY;
                }
            }
        } catch (ParseException ex) {
            throw new MacroException(
                    "Invalid date format, use '"
                    + dateFormat.toPattern() + "' or '"
                    + fullFormat.toPattern() + "'");
        }
        return body;
    }

    /**
     * Suppress surrounding div/span during Wysiwyg rendering.
     *
     * @return  false
     */
    @Override
    public boolean suppressSurroundingTagDuringWysiwygRendering() {
        return false;
    }

    /**
     * Suppress the rendering of the macro -- the macro's body may still be
     * rendered.
     *
     * @return  false
     */
    @Override
    public boolean suppressMacroRenderingDuringWysiwyg() {
        return true;
    }

    /**
     *
     * @return {@link com.atlassian.renderer.v2.macro.WysiwygBodyType#WIKI_MARKUP}
     */
    @Override
    public WysiwygBodyType getWysiwygBodyType() {
        return WysiwygBodyType.WIKI_MARKUP;
    }

    /**
     * Finds a date from input string.
     *
     * @param   dateString
     *          Date string to parse a date for.
     * @return  Parsed date or null if empty string supplied.
     * @throws  ParseException
     *          If the date could not be parsed.
     */
    private Date getDate(final String dateString) throws ParseException {
        if (dateString == null || dateString.length() == 0) {
            return null;
        }

        try {
            return fullFormat.parse(dateString);
        } catch (ParseException pe) {
            try {
                return isoFullFormat.parse(dateString);
            } catch (ParseException pe2) {
                try {
                    return dateFormat.parse(dateString);
                } catch (ParseException pe3) {
                    return isoDateFormat.parse(dateString);
                }
            }
        }
    }

    @Override
    public String execute(
            Map<String, String> parameters,
            String body,
            ConversionContext context)
            throws MacroExecutionException {
        try {
            return execute(parameters, body, (RenderContext) null);
        } catch (MacroException e) {
            throw new MacroExecutionException(e);
        }
    }

    @Override
    public BodyType getBodyType() {
        return BodyType.RICH_TEXT;
    }

    @Override
    public OutputType getOutputType() {
        return OutputType.BLOCK;
    }

}
