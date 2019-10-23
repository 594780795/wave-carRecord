package com.wave.carRecord.service.impl;

import com.sun.scenario.effect.impl.prism.ps.PPStoPSWDisplacementMapPeer;
import com.wave.carRecord.bean.carGate.Gaterecord;
import com.wave.carRecord.bean.uts.ViewBpmFata28;
import com.wave.carRecord.common.*;
import com.wave.carRecord.dao.carGate.GaterecordMapper;
import com.wave.carRecord.dao.uts.ViewBpmFata28Mapper;
import com.wave.carRecord.enums.ExportTypeEnum;
import com.wave.carRecord.service.GaterecordService;
import com.wave.carRecord.util.ExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author mintc
 * @projectName car-record
 * @description: TODO
 * @date 2019/10/2017:43
 */
@Service
public class GaterecordServiceImpl implements GaterecordService {
    @Autowired
    private GaterecordMapper gaterecordMapper;
    
    @Autowired
    private ViewBpmFata28Mapper viewBpmFata28Mapper;
    
    @Autowired
    private ValidCodeConfig validCodeConfig;
    
    @Override
    public ObjectResult<GaterecordResponse> queryCarRecord(QueryRequest req) throws ParseException {
        //1.验证密码  2.查询车闸终端 3.根据车牌查询uts白名单获取姓名 4.封装数据
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        if (StringUtils.isEmpty(req.getPlate())) {
            req.setPlate(null);
        }
        
        //1.验证密码
        if (!validCodeConfig.getCode().equals(req.getValidCode())) {
            return ObjectResult.newFailure(CodeMsg.ERROR_VALID);
        }
        
        //2.查询车闸终端
        GaterecordQueryDTO gaterecordDTO = new GaterecordQueryDTO();
        gaterecordDTO.setPageNumber((req.getPageNumber() - 1) * req.getPageSize());
        gaterecordDTO.setPageSize(req.getPageSize());
        
        if (StringUtils.isNotEmpty(req.getEndDate())) {
            gaterecordDTO.setEndDate(sdf.parse(req.getEndDate()));
        }
        if (StringUtils.isNotEmpty(req.getStartDate())) {
            gaterecordDTO.setStartDate(sdf.parse(req.getStartDate()));
        }
        gaterecordDTO.setPlate(req.getPlate());
        List<Gaterecord> gaterecordList =  gaterecordMapper.selectPageSelective(gaterecordDTO);
        System.out.println("gaterecordMapper.selectByPrimaryKey size: " + gaterecordList.size());
    
        //3.根据车牌查询uts白名单获取姓名
        List<GaterecordVO> gaterecordResponses = new ArrayList<>();
        gaterecordList.forEach(temp -> {
            //响应实体
            GaterecordVO gaterecordResponse = new GaterecordVO();
            gaterecordResponse.setTimestamp(temp.getTimestamp());
            gaterecordResponse.setPlate(temp.getPlate());
            gaterecordResponse.setType(temp.getType());
            gaterecordResponse.setAuthentication(temp.getAuthentication());
            gaterecordResponse.setId(temp.getId());
            //通过UTS的白名单申请表，查询当时车辆进出时的车主
            ViewBpmFata28QueryDTO viewBpmFata28QueryDTO = new ViewBpmFata28QueryDTO();
            viewBpmFata28QueryDTO.setData_m664(temp.getPlate());
            viewBpmFata28QueryDTO.setTimestamp(temp.getTimestamp());
            List<ViewBpmFata28> viewBpmFata28List = viewBpmFata28Mapper.selectSelective(viewBpmFata28QueryDTO);
            if (viewBpmFata28List.size() > 0) {
                gaterecordResponse.setName(viewBpmFata28List.get(0).getData_m662());
            }
            gaterecordResponses.add(gaterecordResponse);
        });
    
        //4.封装数据
        GaterecordResponse gaterecordResponse = new GaterecordResponse();
        gaterecordResponse.setGaterecordVOList(gaterecordResponses);
        int total = gaterecordMapper.selectSelective(gaterecordDTO).size();
        gaterecordResponse.setTotal(total);
        gaterecordResponse.setCurrPage(req.getPageNumber());
        gaterecordResponse.setTotalPage(total%req.getPageSize() == 0 ? total/req.getPageSize() : total/req.getPageSize() + 1);
        gaterecordResponse.setCurrCount(gaterecordList.size());
        
        return ObjectResult.newSuccess(gaterecordResponse);
    }
    
    @Override
    public void exportCarRecordExcel(QueryRequest req, HttpServletResponse response) throws Exception {
        SimpleDateFormat smf_yyyyMMddHHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat smf_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
        
        ExcelUtil downloadUtil = new ExcelUtil();
        //1. 获取要导出的所有数据
    
        //查询车闸终端请求参数
        if (StringUtils.isEmpty(req.getPlate())) {
            req.setPlate(null);
        }
    
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
        //响应实体列表
        List<GaterecordVO> gaterecordResponses = new ArrayList<>();
        //查询车闸终端请求参数
        GaterecordQueryDTO gaterecordDTO = new GaterecordQueryDTO();
        gaterecordDTO.setPageNumber((req.getPageNumber() - 1) * req.getPageSize());
        gaterecordDTO.setPageSize(req.getPageSize());
    
        if (StringUtils.isNotEmpty(req.getEndDate())) {
            gaterecordDTO.setEndDate(sdf.parse(req.getEndDate()));
        }
        if (StringUtils.isNotEmpty(req.getStartDate())) {
            gaterecordDTO.setStartDate(sdf.parse(req.getStartDate()));
        }
        gaterecordDTO.setPlate(req.getPlate());
    
        List<Gaterecord> gaterecordList = new ArrayList<>();
        if (ExportTypeEnum.ALL.getCode().equals(req.getExportType())) {
            gaterecordList =  gaterecordMapper.selectSelective(gaterecordDTO);
        } else {
            gaterecordList =  gaterecordMapper.selectPageSelective(gaterecordDTO);
        }
        
        System.out.println("gaterecordMapper.selectByPrimaryKey size: " + gaterecordList.size());
    
        gaterecordList.forEach(temp -> {
            //响应实体
            GaterecordVO gaterecordResponse = new GaterecordVO();
            gaterecordResponse.setTimestamp(temp.getTimestamp());
            gaterecordResponse.setPlate(temp.getPlate());
            gaterecordResponse.setType(temp.getType());
            gaterecordResponse.setAuthentication(temp.getAuthentication());
            gaterecordResponse.setId(temp.getId());
            //通过UTS的白名单申请表，查询当时车辆进出时的车主
            ViewBpmFata28QueryDTO viewBpmFata28QueryDTO = new ViewBpmFata28QueryDTO();
            viewBpmFata28QueryDTO.setData_m664(temp.getPlate());
            viewBpmFata28QueryDTO.setTimestamp(temp.getTimestamp());
            List<ViewBpmFata28> viewBpmFata28List = viewBpmFata28Mapper.selectSelective(viewBpmFata28QueryDTO);
            if (viewBpmFata28List.size() > 0) {
                gaterecordResponse.setName(viewBpmFata28List.get(0).getData_m662());
            }
            gaterecordResponses.add(gaterecordResponse);
        });
    
        //遍历生成excel
        if (gaterecordResponses != null && gaterecordResponses.size() > 0) {
            //1.创建工作簿 HSSFWorkbook 2003   XSSFWorkbook  2007
            Workbook wb = new XSSFWorkbook();
            //2.创建工作表
            Sheet sheet = wb.createSheet();
        
            //设置列宽
            sheet.setColumnWidth(0, 10 * 256);
            sheet.setColumnWidth(1, 20 * 256);
            sheet.setColumnWidth(2, 20 * 256);
            sheet.setColumnWidth(3, 20 * 256);
            sheet.setColumnWidth(4, 20 * 256);
            sheet.setColumnWidth(5, 20 * 256);
            sheet.setColumnWidth(6, 20 * 256);
        
            //定义公共变量
            int rowNo =0, cellNo = 0;
            Row nRow = null;
            Cell nCell = null;
        
            //大标题
            //3 创建行
            nRow = sheet.createRow(rowNo);
            //设置行高
            nRow.setHeightInPoints(36);
        
            //4 创建单元格
            nCell = nRow.createCell(cellNo);
            //5 设置内容
            if (StringUtils.isNotEmpty(req.getStartDate()) && StringUtils.isNotEmpty(req.getEndDate())) {
                nCell.setCellValue("车辆进出记录   " + req.getStartDate() + "到" + req.getEndDate());
            } else {
                nCell.setCellValue("车辆进出记录");
            }
            //6 设置内容格式
            //合并单元格
            //参数1 ：起始行 参数2 ： 终止行 参数3 ：起始列 参数4 ：终止列
            sheet.addMergedRegion(new CellRangeAddress(0, 0, (short) 0, (short) 6));
        
            //横向居中 + 水平居中 + 红色宋体22号
            nCell.setCellStyle(downloadUtil.bigTitleCellStyle(wb));
        
        
            //小标题输出
            //行号rowNo需要变化么 列需要变化么
            rowNo++;
            String[] titles = {"序号", "编号", "车主姓名", "车牌号", "出入口类型", "来源", "时间"};
            //3 创建行
            nRow = sheet.createRow(rowNo);
            for (String title : titles) {
                //4 创建单元格
                //先创建cell单元格，然后在自增
                nCell = nRow.createCell(cellNo++);
                //5 设置内容
                nCell.setCellValue(title);
                //6 设置内容格式
                nCell.setCellStyle(downloadUtil.titleCellStyle(wb));
            }
        
            //内容
            //行号和列号需要变化么
            rowNo++;
        
            int i = 0;
            //2. 封装excel表格
            for (GaterecordVO gaterecordVO : gaterecordResponses) {
                cellNo = 0;
                //3 创建行
                nRow = sheet.createRow(rowNo++);
            
                nCell = nRow.createCell(cellNo++);
                nCell.setCellValue(++i + "");
                nCell.setCellStyle(downloadUtil.contentCellStyle(wb));
            
                //4 创建单元格
                //5 设置内容  编号id
                nRow = downloadUtil.setCell(nRow, cellNo++, gaterecordVO.getId() + "", downloadUtil.contentCellStyle(wb));
                //6 设置内容格式
                nCell.setCellStyle(downloadUtil.contentCellStyle(wb));
            
                //车主姓名
                nCell = nRow.createCell(cellNo++);
                nCell.setCellValue(gaterecordVO.getName());
                nCell.setCellStyle(downloadUtil.contentCellStyle(wb));
            
                //车牌号
                nCell = nRow.createCell(cellNo++);
                nCell.setCellValue(gaterecordVO.getPlate());
                nCell.setCellStyle(downloadUtil.contentCellStyle(wb));
            
                //出入口
                nRow = downloadUtil.setCell(nRow, cellNo++, gaterecordVO.getType(), downloadUtil.contentCellStyle(wb));
            
                //来源
                nRow = downloadUtil.setCell(nRow, cellNo++, gaterecordVO.getAuthentication(), downloadUtil.contentCellStyle(wb));
            
                //创建时间
                nRow = downloadUtil.setCell(nRow, cellNo++, smf_yyyyMMddHHmmss.format(gaterecordVO.getTimestamp()), downloadUtil.contentCellStyle(wb));
            }
            i = 0;
        
            //下载
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //将wb写入流
            wb.write(byteArrayOutputStream);
    
           down(byteArrayOutputStream);
            
            //3. 返回excel文件
            downloadUtil.download(byteArrayOutputStream, response, "出入记录");
        }
    }
    
    /**
     * 临时文件
     * @param byteArrayOutputStream
     */
    public void down(ByteArrayOutputStream byteArrayOutputStream) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\test\\test.xlsx");
            fileOutputStream.write(byteArrayOutputStream.toByteArray());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
