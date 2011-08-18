/*******************************************************************************
 * Copyright (c) 2011 Ubiquitous Knowledge Processing Lab
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Project Website:
 * 	http://jwpl.googlecode.com
 *
 * Contributors:
 * 	Torsten Zesch
 * 	Simon Kulessa
 * 	Oliver Ferschke
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.codec.RevisionCodecData;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.TaskTypes;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.DiffPart;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.info.ArticleInformation;

/**
 * The WikipediaXMLWriter writes xml representations of task objects to an
 * output file.
 *
 * This class is used for debug purposes.
 *
 *
 *
 */
public class WikipediaXMLWriter
{

	/** Reference to the writer */
	private final OutputStreamWriter writer;

	/**
	 * (Constructor) Creates a WikipediaXMLWriter object.
	 *
	 * @param path
	 *            path of the output file
	 * @throws IOException
	 *             if an error occurs while writing the output
	 */
	public WikipediaXMLWriter(final String path)
		throws IOException
	{
		this.writer = new OutputStreamWriter(new FileOutputStream(path),
				"UTF-8");
	}

	/**
	 * Writes the diff task to the output using wikipedia xml notation.
	 *
	 * @param diff
	 *            Reference to a diff task
	 * @throws IOException
	 *             if an error occurs while writing the output
	 */
	public void writeDiff(final Task<Diff> diff)
		throws IOException
	{
		writeDiff(diff, 0);
	}

	/**
	 * Writes a part of the diff task, starting with the given element, to the
	 * output using wikipedia xml notation.
	 *
	 * @param diff
	 *            Reference to a diff task
	 * @param start
	 *            Position of the start element
	 * @throws IOException
	 *             if an error occurs while writing the output
	 */
	public void writeDiff(final Task<Diff> diff, final int start)
		throws IOException
	{

		int size = diff.size();
		Diff d;
		String previousRevision = null, currentRevision = null;

		this.writer
				.write(WikipediaXMLKeys.KEY_START_PAGE.getKeyword() + "\r\n");

		ArticleInformation header = diff.getHeader();

		this.writer.write("\t" + WikipediaXMLKeys.KEY_START_TITLE.getKeyword());
		this.writer.write(header.getArticleName());
		this.writer.write(WikipediaXMLKeys.KEY_END_TITLE.getKeyword() + "\r\n");

		this.writer.write("\t" + WikipediaXMLKeys.KEY_START_ID.getKeyword());
		this.writer.write(Integer.toString(header.getArticleId()));
		this.writer.write(WikipediaXMLKeys.KEY_END_ID.getKeyword() + "\r\n");

		this.writer.write("\t<partCounter>");
		this.writer.write(Integer.toString(diff.getPartCounter()));
		this.writer.write("</partCounter>\r\n");

		for (int i = start; i < size; i++) {
			d = diff.get(i);
			currentRevision = d.buildRevision(previousRevision);

			this.writer
					.write("\t"
							+ WikipediaXMLKeys.KEY_START_REVISION.getKeyword()
							+ "\r\n");

			this.writer.write("\t\t"
					+ WikipediaXMLKeys.KEY_START_ID.getKeyword());
			this.writer.write(Integer.toString(d.getRevisionID()));
			this.writer
					.write(WikipediaXMLKeys.KEY_END_ID.getKeyword() + "\r\n");

			this.writer.write("\t\t<revCount>");
			this.writer.write(Integer.toString(d.getRevisionCounter()));
			this.writer.write("</revCount>\r\n");

			this.writer.write("\t\t"
					+ WikipediaXMLKeys.KEY_START_TIMESTAMP.getKeyword());
			this.writer.write(d.getTimeStamp().toString());
			this.writer.write(WikipediaXMLKeys.KEY_END_TIMESTAMP.getKeyword()
					+ "\r\n");

			this.writer.write("\t\t"
					+ WikipediaXMLKeys.KEY_START_TEXT.getKeyword());
			if (currentRevision != null) {
				this.writer.write(currentRevision);
				previousRevision = currentRevision;
			}
			this.writer.write(WikipediaXMLKeys.KEY_END_TEXT.getKeyword()
					+ "\r\n");

			this.writer.write("\t"
					+ WikipediaXMLKeys.KEY_END_REVISION.getKeyword() + "\r\n");

		}

		this.writer.write(WikipediaXMLKeys.KEY_END_PAGE.getKeyword() + "\r\n");
		this.writer.flush();
	}

	/**
	 * Writes the diff task to the output using an xml representation of the
	 * diff information.
	 *
	 * @param diff
	 *            Reference to a diff task
	 * @throws IOException
	 *             if an error occurs while writing the output
	 */
	public void writeDiffFile(final Task<Diff> diff)
		throws IOException
	{

		int partsCount;
		int size = diff.size();
		Diff d;
		DiffPart p;
		RevisionCodecData codec;

		this.writer
				.write(WikipediaXMLKeys.KEY_START_PAGE.getKeyword() + "\r\n");

		ArticleInformation header = diff.getHeader();

		this.writer.write("\t" + WikipediaXMLKeys.KEY_START_TITLE.getKeyword());
		this.writer.write(header.getArticleName());
		this.writer.write(WikipediaXMLKeys.KEY_END_TITLE.getKeyword() + "\r\n");

		this.writer.write("\t" + WikipediaXMLKeys.KEY_START_ID.getKeyword());
		this.writer.write(Integer.toString(header.getArticleId()));
		this.writer.write(WikipediaXMLKeys.KEY_END_ID.getKeyword() + "\r\n");

		this.writer.write("\t<partCounter>");
		this.writer.write(Integer.toString(diff.getPartCounter()));
		this.writer.write("</partCounter>\r\n");

		for (int i = 0; i < size; i++) {
			d = diff.get(i);

			this.writer
					.write("\t"
							+ WikipediaXMLKeys.KEY_START_REVISION.getKeyword()
							+ "\r\n");

			codec = d.getCodecData();
			if (!codec.isConverted()) {
				codec.totalSizeInBits();
			}

			this.writer.write("\t\t<codecData>\r\n");

			this.writer.write("\t\t\t<s>"
					+ Integer.toString(codec.getBlocksizeS()) + "</s>\r\n");
			this.writer.write("\t\t\t<e>"
					+ Integer.toString(codec.getBlocksizeE()) + "</e>\r\n");
			this.writer.write("\t\t\t<b>"
					+ Integer.toString(codec.getBlocksizeB()) + "</b>\r\n");
			this.writer.write("\t\t\t<l>"
					+ Integer.toString(codec.getBlocksizeL()) + "</l>\r\n");

			this.writer.write("\t\t</codecData>\r\n");

			this.writer.write("\t\t"
					+ WikipediaXMLKeys.KEY_START_ID.getKeyword());
			this.writer.write(Integer.toString(d.getRevisionID()));
			this.writer
					.write(WikipediaXMLKeys.KEY_END_ID.getKeyword() + "\r\n");

			this.writer.write("\t\t<revCount>");
			this.writer.write(Integer.toString(d.getRevisionCounter()));
			this.writer.write("</revCount>\r\n");

			this.writer.write("\t\t"
					+ WikipediaXMLKeys.KEY_START_TIMESTAMP.getKeyword());
			this.writer.write(d.getTimeStamp().toString());
			this.writer.write(WikipediaXMLKeys.KEY_END_TIMESTAMP.getKeyword()
					+ "\r\n");

			this.writer.write("\t\t<diff>\r\n");
			partsCount = d.size();
			for (int j = 0; j < partsCount; j++) {

				p = d.get(j);
				this.writer.write("\t\t\t<diffPart>\r\n");

				this.writer.write("\t\t\t\t<action>" + p.getAction()
						+ "</action>\r\n");
				this.writer.write("\t\t\t\t<start>"
						+ Integer.toString(p.getStart()) + "</start>\r\n");
				this.writer.write("\t\t\t\t<end>"
						+ Integer.toString(p.getEnd()) + "</end>\r\n");
				if (p.getText() != null) {
					this.writer
							.write("\t\t\t\t<content xml:space=\"preserve\">"
									+ p.getText());
					this.writer.write("</content>\r\n");
				}

				this.writer.write("\t\t\t</diffPart>\r\n");
			}

			this.writer.write("\t\t</diff>\r\n");
			this.writer.write("\t"
					+ WikipediaXMLKeys.KEY_END_REVISION.getKeyword() + "\r\n");
		}

		this.writer.write(WikipediaXMLKeys.KEY_END_PAGE.getKeyword() + "\r\n");
		this.writer.flush();
	}

	/**
	 * Writes the revision task to the output using wikipedia xml notation.
	 *
	 * @param task
	 *            Reference to a revision task
	 * @throws IOException
	 *             if an error occurs while writing the output
	 */
	public void writeRevision(final Task<Revision> task)
		throws IOException
	{

		if (task.getTaskType() == TaskTypes.TASK_PARTIAL_FIRST
				|| task.getTaskType() == TaskTypes.TASK_FULL) {

			this.writer.write(WikipediaXMLKeys.KEY_START_PAGE.getKeyword()
					+ "\r\n");

			ArticleInformation header = task.getHeader();

			this.writer.write("\t"
					+ WikipediaXMLKeys.KEY_START_TITLE.getKeyword());
			this.writer.write(header.getArticleName());
			this.writer.write(WikipediaXMLKeys.KEY_END_TITLE.getKeyword()
					+ "\r\n");

			this.writer
					.write("\t" + WikipediaXMLKeys.KEY_START_ID.getKeyword());
			this.writer.write(Integer.toString(header.getArticleId()));
			this.writer
					.write(WikipediaXMLKeys.KEY_END_ID.getKeyword() + "\r\n");
		}

		Revision rev;
		Iterator<Revision> revIt = task.iterator();
		while (revIt.hasNext()) {

			this.writer
					.write("\t"
							+ WikipediaXMLKeys.KEY_START_REVISION.getKeyword()
							+ "\r\n");
			rev = revIt.next();

			this.writer.write("\t\t"
					+ WikipediaXMLKeys.KEY_START_ID.getKeyword());
			this.writer.write(Integer.toString(rev.getRevisionID()));
			this.writer
					.write(WikipediaXMLKeys.KEY_END_ID.getKeyword() + "\r\n");

			this.writer.write("\t\t<revCount>");
			this.writer.write(Integer.toString(rev.getRevisionCounter()));
			this.writer.write("</revCount>\r\n");

			this.writer.write("\t\t"
					+ WikipediaXMLKeys.KEY_START_TIMESTAMP.getKeyword());
			this.writer.write(rev.getTimeStamp().toString());
			this.writer.write(WikipediaXMLKeys.KEY_END_TIMESTAMP.getKeyword()
					+ "\r\n");

			this.writer.write("\t\t"
					+ WikipediaXMLKeys.KEY_START_CONTRIBUTOR.getKeyword());
			if(rev.contributorIsRegistered()){
				this.writer.write("\t\t"
						+ WikipediaXMLKeys.KEY_START_USERNAME.getKeyword());
				this.writer.write(rev.getContributorName());
				this.writer.write(WikipediaXMLKeys.KEY_END_USERNAME.getKeyword()
						+ "\r\n");

				this.writer.write("\t\t"
						+ WikipediaXMLKeys.KEY_START_ID.getKeyword());
				this.writer.write(rev.getContributorId());
				this.writer.write(WikipediaXMLKeys.KEY_END_ID.getKeyword()
						+ "\r\n");
			}
			else{
				this.writer.write("\t\t"
						+ WikipediaXMLKeys.KEY_START_IP.getKeyword());
				this.writer.write(rev.getContributorName());
				this.writer.write(WikipediaXMLKeys.KEY_END_IP.getKeyword()
						+ "\r\n");
			}

			this.writer.write(WikipediaXMLKeys.KEY_END_CONTRIBUTOR.getKeyword()
					+ "\r\n");

			if(rev.isMinor()){
				this.writer.write("\t\t"+WikipediaXMLKeys.KEY_MINOR_FLAG.getKeyword()
						+ "\r\n");
			}

			this.writer.write("\t\t"
					+ WikipediaXMLKeys.KEY_START_COMMENT.getKeyword());
			this.writer.write(rev.getComment());
			this.writer.write(WikipediaXMLKeys.KEY_END_COMMENT.getKeyword()
					+ "\r\n");




			this.writer.write("\t\t"
					+ WikipediaXMLKeys.KEY_START_TEXT.getKeyword());
			if (rev.getRevisionText() != null) {
				this.writer.write(rev.getRevisionText());
			}
			this.writer.write(WikipediaXMLKeys.KEY_END_TEXT.getKeyword()
					+ "\r\n");

			this.writer.write("\t"
					+ WikipediaXMLKeys.KEY_END_REVISION.getKeyword() + "\r\n");
		}

		if (task.getTaskType() == TaskTypes.TASK_PARTIAL_LAST
				|| task.getTaskType() == TaskTypes.TASK_FULL) {

			this.writer.write(WikipediaXMLKeys.KEY_END_PAGE.getKeyword()
					+ "\r\n");
		}
		this.writer.flush();
	}

	/**
	 * Closes the writer.
	 *
	 * @throws IOException
	 *             if an error occured while closing the writer
	 */
	public void close()
		throws IOException
	{
		this.writer.close();
	}
}
