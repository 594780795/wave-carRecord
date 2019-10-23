package com.wave.carRecord.util;


import com.wave.carRecord.common.ExcelSheetPO;
import com.wave.carRecord.common.NumberConstants;
import com.wave.carRecord.enums.ExcelVersionEnum;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * excel工具类
 * 提供读取和写入excel的功能
 * @author: lenovo
 * @date: 2019/5/31 15:48
 */
public class ExcelUtil {

    /**
     * 标题样式
     */
    private static final String STYLE_HEADER = "header";
    /**
     * 表头样式
     */
    private static final String STYLE_TITLE = "title";
    /**
     * 数据样式
     */
    private static final String STYLE_DATA = "data";
    /**
     * 存储格式
     */
    private static final HashMap<String, CellStyle> cellStyleMap = new HashMap<>();

    /**
     * 获取文件后缀名
     * @param file
     * @return
     */
    public static String getFileSuffix(File file) {
        String fileName = file.getName();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        return suffix;
    }


    /**
     * 解析excel文件里面的内容，支持日期，数字，字符，函数公式，布尔类型
     * @param file
     * @param rowCount
     * @param columnCount
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<ExcelSheetPO> readExcel(File file, Integer rowCount, Integer columnCount)
        throws FileNotFoundException, IOException {
        //根据后缀名判断excel版本
        String extName = getFileSuffix(file);
        Workbook wb = null;
        if (ExcelVersionEnum.V2003.getSuffix().equals(extName)) {
            wb = new HSSFWorkbook(new FileInputStream(file));
        } else if (ExcelVersionEnum.V2007.getSuffix().equals(extName)) {
            wb = new XSSFWorkbook(new FileInputStream(file));
        } else {
            throw new IllegalArgumentException("invalid excel version");
        }

        List<ExcelSheetPO> sheetPOs = new ArrayList<>();
        //解析sheet
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet sheet = wb.getSheetAt(i);
            List<List<Object>> dataList = new ArrayList<>();
            ExcelSheetPO sheetPO = new ExcelSheetPO();
            sheetPO.setSheetName(sheet.getSheetName());
            sheetPO.setDataList(dataList);
            int readRowCount = 0;
            if (rowCount == null || rowCount > sheet.getPhysicalNumberOfRows()) {
                readRowCount = sheet.getPhysicalNumberOfRows();
            } else {
                readRowCount = rowCount;
            }

            //解析sheet的行
            for (int j = sheet.getFirstRowNum(); j < readRowCount; j++) {
                Row row = sheet.getRow(j);
                if (row == null) {
                    continue;
                }
                if (row.getFirstCellNum() < 0) {
                    continue;
                }

                int readColumnCount = 0;
                if (columnCount == null || columnCount > row.getLastCellNum()) {
                    readColumnCount = (int) row.getLastCellNum();
                } else {
                    readColumnCount = columnCount;
                }
                List<Object> rowValue = new LinkedList<>();
                //解析sheet的列
                for (int k = 0; k < readColumnCount; k++) {
                    Cell cell = row.getCell(k);
                    rowValue.add(getCellValue(wb, cell));
                }
                dataList.add(rowValue);
            }
            sheetPOs.add(sheetPO);
        }
        return sheetPOs;
    }


    /**
     *
     * @param wb
     * @param cell
     * @return
     */
    private static Object getCellValue(Workbook wb, Cell cell) {
        Object columnValue = null;
        if (cell != null) {
            //格式化number
            DecimalFormat df = new DecimalFormat("0");
            //字符 格式化日期字符串
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //格式化数字
            DecimalFormat nf = new DecimalFormat("0.00");
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    columnValue = cell.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    if ("@".equals(cell.getCellStyle().getDataFormatString())) {
                        columnValue = df.format(cell.getNumericCellValue());
                    } else if ("General".equals(cell.getCellStyle().getDataFormatString())) {
                        columnValue = nf.format(cell.getNumericCellValue());
                    } else {
                        columnValue = sdf.format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()));
                    }
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    columnValue = cell.getBooleanCellValue();
                    break;
                case Cell.CELL_TYPE_BLANK:
                    columnValue = "";
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    //格式单元格
                    FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
                    evaluator.evaluateFormulaCell(cell);
                    CellValue cellValue = evaluator.evaluate(cell);
                    columnValue = cellValue.getNumberValue();
                    break;
                default:
                    columnValue = cell.toString();
            }
        }
        return columnValue;
    }


    /**
     * 在硬盘写入excel文件
     * @param version
     * @param excelSheets
     * @param filePath
     * @throws IOException
     */
    public static void createWorkbookAtDisk(ExcelVersionEnum version, List<ExcelSheetPO> excelSheets, String filePath)
            throws IOException{
        FileOutputStream fileOut = new FileOutputStream(filePath);
        createWorkbookAtOutStream(version, excelSheets, fileOut, true);
    }


    /**
     * 把excel表格写入输出流中，输出流会被关闭
     * @param version
     * @param excelSheets
     * @param outputStream
     * @param closeStream 是否关闭输出流
     * @throws IOException
     */
    public static void createWorkbookAtOutStream(ExcelVersionEnum version, List<ExcelSheetPO> excelSheets,
                                                 OutputStream outputStream, boolean closeStream) throws IOException {
        if (!CollectionUtils.isEmpty(excelSheets)) {
            Workbook wb = createWorkBook(version, excelSheets);
            wb.write(outputStream);
            if (closeStream) {
                outputStream.close();
            }
        }
    }


    private static Workbook createWorkBook(ExcelVersionEnum version, List<ExcelSheetPO> excelSheets) {
        Workbook wb = createWorkbook(version);
        for (int i = 0; i < excelSheets.size(); i++) {
            ExcelSheetPO excelSheetPO = excelSheets.get(i);
            if (excelSheetPO.getSheetName() == null) {
                excelSheetPO.setSheetName("sheet" + i);
            }
            //过滤特殊字符
            Sheet tempSheet = wb.createSheet(WorkbookUtil.createSafeSheetName(excelSheetPO.getSheetName()));
            buildSheetData(wb, tempSheet, excelSheetPO, version);
        }
        return wb;
    }

    private static void buildSheetData(Workbook wb, Sheet sheet, ExcelSheetPO excelSheetPO,
                                       ExcelVersionEnum version) {
        sheet.setDefaultRowHeight((short) 400);
        sheet.setDefaultColumnWidth((short) 10);
        createTile(sheet, excelSheetPO, wb, version);
        createHeader(sheet, excelSheetPO, wb, version);
        createBody(sheet, excelSheetPO, wb, version);
    }

    private static void createBody(Sheet sheet, ExcelSheetPO excelSheetPO, Workbook wb,
                                   ExcelVersionEnum version) {
        List<List<Object>> dataList = excelSheetPO.getDataList();
        for (int i = 0; i < dataList.size() && i < version.getMaxRow(); i++) {
            List<Object> values = dataList.get(i);
            Row row = sheet.createRow(2 + i);
            for (int j = 0; j < values.size() && j < version.getMaxColumn(); j++) {
                Cell cell = row.createCell(j);
                cell.setCellStyle(getStyle(STYLE_DATA, wb));
                cell.setCellValue(values.get(j).toString());
            }
        }
    }

    private static void createHeader(Sheet sheet, ExcelSheetPO excelSheetPO, Workbook wb, ExcelVersionEnum version) {
        String[] headers  = excelSheetPO.getHeaders();
        Row row = sheet.createRow(1);
        for (int i = 0; i < headers.length && i < version.getMaxColumn(); i++) {
            Cell cellHeader = row.createCell(i);
            cellHeader.setCellStyle(getStyle(STYLE_HEADER, wb));
            cellHeader.setCellValue(headers[i]);
        }
    }


    private static void createTile(Sheet sheet, ExcelSheetPO excelSheetPO, Workbook wb,
                                   ExcelVersionEnum version) {
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(excelSheetPO.getTitle());
        titleCell.setCellStyle(getStyle(STYLE_TITLE, wb));
        //限制最大列数
        int column = excelSheetPO.getDataList().size() > version.getMaxColumn() ?
                version.getMaxColumn() : excelSheetPO.getDataList().size();
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0 , column - 1));
    }


    /**
     * 获取样式
     * @param type
     * @param wb
     * @return
     */
    private static CellStyle getStyle(String type, Workbook wb) {
        if (cellStyleMap.containsKey(type)) {
            return cellStyleMap.get(type);
        }

        //生成一个样式
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setWrapText(true);

        if (STYLE_HEADER == type) {
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            Font font = wb.createFont();
            font.setFontHeightInPoints((short) 16);
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            style.setFont(font);
        } else if (STYLE_TITLE  == type) {
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            Font font = wb.createFont();
            font.setFontHeightInPoints((short) 18);
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            style.setFont(font);
        } else if (STYLE_DATA == type) {
            style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            Font font = wb.createFont();
            font.setFontHeightInPoints((short) 12);
            style.setFont(font);
        }
        cellStyleMap.put(type, style);
        return style;
    }



    private static Workbook createWorkbook(ExcelVersionEnum version) {
        switch (version) {
            case V2003:
                return new HSSFWorkbook();
            case V2007:
                return new XSSFWorkbook();
        }
        return null;
    }




    /**
     *
     * @param filePath
     * @param returnName
     * @param response
     * @param delFlag
     */
    protected void download(String filePath, String returnName, HttpServletResponse response, boolean delFlag) {
        this.prototypeDownload(new File(filePath), returnName, response, delFlag);
    }

    /**
     *
     * @param file
     * @param returnName
     * @param response
     * @param delFlag
     */
    protected void download(File file, String returnName, HttpServletResponse response, boolean delFlag) {
        this.prototypeDownload(file, returnName, response, delFlag);
    }

    /**
     *
     * @param file 要下载的文件
     * @param returnName 返回的文件名
     * @param response
     * @param delFlag 是否删除文件
     */
    public void prototypeDownload(File file, String returnName, HttpServletResponse response, boolean delFlag) {
        //下载文件
        FileInputStream inputStream = null;
        ServletOutputStream outputStream = null;
        try {
            if (!file.exists()) {
                return;
            }
            response.reset();
            //设置响应类型 pdf类型application/pdf  word文件为application/msword
            //excel文件为application/vnd.ms-excel
            response.setContentType("application/octet-stream;charset=utf-8");

            //设置响应的文件名称，并转换成中文编码
            //保存的文件名必须和页面编码一致，否则乱码
            returnName = response.encodeURL(new String(returnName.getBytes(), "iso8859-1"));

            //attachment作为附件下载，inline客户端机器有安装匹配程序，则直接打开，
            // 注意改变配置，清除缓存，否则可能不能看到效果
            response.addHeader("Content-Disposition", "attachment;filename=" + returnName);

            //将文件读入响应流
            inputStream = new FileInputStream(file);
            outputStream = response.getOutputStream();
            int length = 1024;
            int readLength = 0;
            byte buf[] = new byte[1024];
            readLength = inputStream.read(buf, 0, length);
            while (readLength != -1) {
                outputStream.write(buf, 0, readLength);
                readLength = inputStream.read(buf, 0, length);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //删除原文件
            if (delFlag) {
                file.delete();
            }
        }
    }

    /**
     *
     * @param byteArrayOutputStream
     * @param response
     * @param returnName
     * @throws IOException
     */
    public void download(ByteArrayOutputStream byteArrayOutputStream, HttpServletResponse response,
                         String returnName) throws IOException {
//        response.setContentType("application-octet-stream;charset=utf-8");
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
//        response.setContentType("application/vnd.ms-excel;charset=utf-8");
//        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setContentType("application/x-download;charset=utf-8");
        //保存的文件名，必须和页面编码一致，否则乱码
//        returnName = response.encodeURL(new String(returnName.getBytes(), "iso8859-1"));
        returnName = response.encodeURL(new String(returnName.getBytes(), "UTF-8"));
        response.addHeader("Content-Disposition", "attachment;filename=" + returnName + ".xlsx");
        response.setContentLength(byteArrayOutputStream.size());

        //取得输出流
        ServletOutputStream outputStream = response.getOutputStream();
        //写到输出流
        byteArrayOutputStream.writeTo(outputStream);
        //关闭
        byteArrayOutputStream.close();
        outputStream.flush();
    }



    public Row setCell(Row nRow, int num, String content, CellStyle cellStyle) {
        Cell nCell = null;
        nCell = nRow.createCell(num);
        nCell.setCellValue(content);
        nCell.setCellStyle(cellStyle);
        return nRow;
    }


    public CellStyle bigTitleCellStyle(Workbook wb) {
        //横向居中 + 水平居中 + 红色宋体22号
        CellStyle cellStyle = wb.createCellStyle();
        //横向居中
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        //垂直居中
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

        Font font = wb.createFont();
        font.setFontHeight((short) 440);
        font.setColor(Font.COLOR_RED);
        font.setFontName(NumberConstants.FONT_STYLE_SONG);
        cellStyle.setFont(font);
        return cellStyle;
    }


    public CellStyle titleCellStyle(Workbook wb) {
        //宋体16号 倾斜 边框线 水平垂直居中
        Font font = wb.createFont();
        font.setFontName(NumberConstants.FONT_STYLE_SONG);
        font.setItalic(true);
        font.setBold(true);

        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setBorderTop(CellStyle.BORDER_THIN);
        cellStyle.setBorderRight(CellStyle.BORDER_DASHED);
        cellStyle.setBorderBottom(CellStyle.BORDER_DOTTED);
        cellStyle.setBorderLeft(CellStyle.BORDER_DOUBLE);
        //横向居中
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        //垂直居中
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        return cellStyle;
    }


    public CellStyle contentCellStyle(Workbook wb) {
        //边框线 水平垂直居中
        CellStyle cellStyle = wb.createCellStyle();
        //边框线
        cellStyle.setBorderTop(CellStyle.BORDER_THIN);
        cellStyle.setBorderRight(CellStyle.BORDER_THIN);
        cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
        return cellStyle;
    }


    public static void main(String[] args) throws IOException {
        File file = new File("d:\\bos运单表.xlsx");
        List<ExcelSheetPO> sheetPOList = readExcel(file, 10, 10);
        System.out.println(sheetPOList);
        for (ExcelSheetPO sheetPO : sheetPOList) {
            System.out.println("sheetPo: " + sheetPO);
            List<List<Object>> resList = sheetPO.getDataList();
            for (List<Object> objects : resList) {
                System.out.println("objects==" + objects);
                for (Object obj : objects) {
                    System.out.println("obj===" + obj.toString());
                }
            }
        }
    }
}
