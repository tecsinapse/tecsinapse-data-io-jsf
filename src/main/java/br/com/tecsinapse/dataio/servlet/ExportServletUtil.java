/*
 * Tecsinapse Data Input and Output
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later
 * See the LICENSE file in the root directory or <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */

package br.com.tecsinapse.dataio.servlet;

import br.com.tecsinapse.dataio.Table;
import br.com.tecsinapse.dataio.txt.FileTxt;
import br.com.tecsinapse.dataio.type.FileType;
import br.com.tecsinapse.dataio.util.ExporterUtil;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static br.com.tecsinapse.dataio.type.FileType.TXT;
import static br.com.tecsinapse.dataio.type.SeparatorType.SEMICOLON;

public final class ExportServletUtil {

    private ExportServletUtil() {
    }

    public static void configureContentAndHeaders(String filename, FileType fileType, HttpServletResponse response) {
        response.setContentType(fileType.getMimeType());
        response.setHeader("Expires", "0");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setHeader("Content-disposition", "attachment;filename=" + filename);
    }

    public static void facesDownloadResponse(List<Table> tables, String filename, FileType fileType, boolean zipFile) throws IOException {
        facesDownloadResponse(tables, filename, fileType, "UTF-8", zipFile);
    }

    public static void facesDownloadResponse(List<Table> tables, String filename, FileType fileType, String charset, boolean zipFile) throws IOException {
        facesDownloadResponse(tables, filename, fileType, charset, SEMICOLON.getSeparator(), zipFile);
    }

    public static void facesDownloadResponse(List<Table> tables, String filename, FileType fileType, String charset, String separator, boolean zipFile) throws IOException {
        if (zipFile) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ExporterUtil.writeData(tables, fileType, baos, charset, separator);
            facesDownloadZip(baos, filename);
            return;
        }
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        configureContentAndHeaders(filename, fileType, response);
        OutputStream outputStream = response.getOutputStream();
        ExporterUtil.writeData(tables, fileType, outputStream, charset, separator);
        outputStream.close();
        context.responseComplete();
    }

    public static void facesDownloadFileTxt(FileTxt file, String filename, String charset, boolean zipFile) throws IOException {
        if (zipFile) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            facesDownloadZip(baos, filename);
            return;
        }
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        configureContentAndHeaders(filename, TXT, response);
        OutputStream outputStream = response.getOutputStream();
        ExporterUtil.writeFileTxtToOutput(file, charset, outputStream);
        outputStream.close();
        context.responseComplete();
    }

    public static void facesDownloadZip(ByteArrayOutputStream baos, String filename) throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        facesDownloadZip(baos, filename, response);
        context.responseComplete();
    }

    private static void facesDownloadZip(ByteArrayOutputStream baos, String filename, HttpServletResponse response) throws IOException {
        String zipFilename = FileType.ZIP.toFilenameWithExtension(filename);
        configureContentAndHeaders(zipFilename, FileType.ZIP, response);
        OutputStream outputStream = response.getOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            zipOutputStream.putNextEntry(new ZipEntry(filename));
            zipOutputStream.write(baos.toByteArray());
            zipOutputStream.closeEntry();
        }
        outputStream.close();
    }

    public static void facesDownloadXls(String name, Table t) throws IOException {
        facesDownloadMoreThanOneSheetXls(name, Arrays.asList(t));
    }

    public static void facesDownloadMoreThanOneSheetXls(String name, List<Table> t) throws IOException {
        facesDownloadResponse(t, name + ".xls", FileType.XLS, false);
    }

    public static void facesDownloadXlsx(String name, Table t) throws IOException {
        facesDownloadMoreThanOneSheetXlsx(name, Arrays.asList(t));
    }

    public static void facesDownloadMoreThanOneSheetXlsx(String name, List<Table> t) throws IOException {
        facesDownloadResponse(t, name + ".xlsx", FileType.XLSX, false);
    }

    public static void facesDownloadCsv(String name, Table t, String chartsetName) throws IOException {
        String filename = FileType.CSV.toFilenameWithExtensionAndLocalTimeNow(name);
        facesDownloadResponse(Arrays.asList(t), filename, FileType.CSV, chartsetName, false);
    }

    public static void facesDownloadTxt(String name, Table t, String chartsetName) throws IOException {
        String filename = TXT.toFilenameWithExtensionAndLocalTimeNow(name);
        facesDownloadResponse(Arrays.asList(t), filename, TXT, chartsetName, false);
    }

    public static void facesDownloadCsvZip(String name, Table t, String charsetName) throws IOException {
        String filename = FileType.CSV.toFilenameWithExtensionAndLocalTimeNow(name);
        facesDownloadResponse(Arrays.asList(t), filename, FileType.CSV, charsetName, true);
    }

}
