/*
 * Copyright (c) 2020 bahlef.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * Contributors:
 * bahlef - initial API and implementation and/or initial documentation
 */
package de.funfried.netbeans.plugins.external.formatter.javascript.eclipse;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.wst.jsdt.core.ToolFactory;
import org.eclipse.wst.jsdt.core.formatter.CodeFormatter;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

import de.funfried.netbeans.plugins.external.formatter.exceptions.CannotLoadConfigurationException;
import de.funfried.netbeans.plugins.external.formatter.exceptions.ConfigReadException;
import de.funfried.netbeans.plugins.external.formatter.exceptions.FormattingFailedException;
import de.funfried.netbeans.plugins.external.formatter.exceptions.ProfileNotFoundException;

/**
 * Wrapper class to the Eclipse formatter implementation.
 *
 * @author bahlef
 */
public final class EclipseJavascriptFormatterWrapper {
	/** Use to specify the kind of the code snippet to format. */
	private static final int FORMATTER_OPTS = CodeFormatter.K_JAVASCRIPT_UNIT;

	/**
	 * Package private Constructor for creating a new instance of {@link EclipseJavascriptFormatterWrapper}.
	 */
	EclipseJavascriptFormatterWrapper() {
	}

	/**
	 * Formats the given {@code code} with the given configurations and returns
	 * the formatted code.
	 *
	 * @param formatterFile    the path to the formatter configuration file
	 * @param formatterProfile the name of the formatter configuration profile
	 * @param code             the unformatted code
	 * @param lineFeed         the line feed to use for formatting
	 *
	 * @return the formatted code
	 *
	 * @throws ConfigReadException              if there is an issue parsing the formatter configuration
	 * @throws ProfileNotFoundException         if the given {@code profile} could not be found
	 * @throws CannotLoadConfigurationException if there is any issue accessing or reading the formatter configuration
	 * @throws FormattingFailedException        if the external formatter failed to format the given code
	 */
	@CheckForNull
	public String format(String formatterFile, String formatterProfile, String code, String lineFeed)
			throws ConfigReadException, ProfileNotFoundException, CannotLoadConfigurationException, FormattingFailedException {
		if (code == null) {
			return null;
		}

		Map<String, String> allConfig = EclipseFormatterConfig.parseConfig(formatterFile, formatterProfile, null);

		CodeFormatter formatter = ToolFactory.createCodeFormatter(allConfig, ToolFactory.M_FORMAT_EXISTING);

		return format(formatter, code, lineFeed);
	}

	/**
	 * Formats the given {@code code} with the given configurations and returns
	 * the formatted code.
	 *
	 * @param formatter the {@link CodeFormatter}
	 * @param code      the unformatted code
	 * @param lineFeed  the line feed to use for formatting
	 *
	 * @return the formatted code
	 *
	 * @throws FormattingFailedException if the external formatter failed to format the given code
	 */
	@CheckForNull
	private String format(@NonNull CodeFormatter formatter, @NonNull String code, String lineFeed) throws FormattingFailedException {
		String formattedCode = null;

		try {
			TextEdit te = formatter.format(FORMATTER_OPTS, code, 0, code.length(), 0, lineFeed);
			if (te != null && te.getChildrenSize() > 0) {
				IDocument dc = new Document(code);
				te.apply(dc);

				formattedCode = dc.get();

				if (Objects.equals(code, formattedCode)) {
					return null;
				}
			} else {
				throw new FormattingFailedException("Formatting the given code ended in a null result.");
			}
		} catch (FormattingFailedException | IllegalArgumentException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new FormattingFailedException("Failed to format the given code.", ex);
		}

		return formattedCode;
	}
}
