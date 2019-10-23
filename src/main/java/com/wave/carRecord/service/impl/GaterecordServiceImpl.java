package com.wave.carRecord.service.impl;

import com.alibaba.fastjson.JSON;
import com.sun.scenario.effect.impl.prism.ps.PPStoPSWDisplacementMapPeer;
import com.wave.carRecord.bean.carGate.Gaterecord;
import com.wave.carRecord.bean.uts.ViewBpmFata28;
import com.wave.carRecord.common.*;
import com.wave.carRecord.controller.CarRecordController;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author mintc
 * @projectName car-record
 * @description: TODO
 * @date 2019/10/2017:43
 */
@Service
public class GaterecordServiceImpl implements GaterecordService {
    
    private final Logger logger = LoggerFactory.getLogger(GaterecordService.class);
    
    /**
     * 一次读取的数据量
     */
    private final static int ONCE_READ_COUNT = 200;
    
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
            logger.error("密码[{}]错误", req.getValidCode());
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
        logger.info("gaterecordMapper.selectByPrimaryKey prams: ", JSON.toJSONString(gaterecordDTO));
        List<Gaterecord> gaterecordList =  gaterecordMapper.selectPageSelective(gaterecordDTO);
        logger.info("gaterecordMapper.selectByPrimaryKey size: ", gaterecordList.size());
    
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
    
    
        GaterecordResponse logReponse = new GaterecordResponse();
        BeanUtils.copyProperties(gaterecordResponse, logReponse);
        logReponse.setGaterecordVOList(null);
        logger.info("gaterecordResponse : [{}] ", + gaterecordList.size());
        return ObjectResult.newSuccess(gaterecordResponse);
    }
    
    @Override
    public void exportCarRecordExcel(QueryRequest req, HttpServletResponse response) throws Exception {
        SimpleDateFormat smf_yyyyMMddHHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat smf_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
        
        ExcelUtil downloadUtil = new ExcelUtil();
    
        //查询车闸终端请求参数
        if (StringUtils.isEmpty(req.getPlate())) {
            req.setPlate(null);
        }
    
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
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
        logger.info("gaterecordMapper.selectByPrimaryKey params: [{}]", JSON.toJSONString(gaterecordDTO));
        if (ExportTypeEnum.ALL.getCode().equals(req.getExportType())) {
            logger.info("导出全部");
            gaterecordList =  gaterecordMapper.selectSelective(gaterecordDTO);
        } else {
            logger.info("导出当前页");
            gaterecordList =  gaterecordMapper.selectPageSelective(gaterecordDTO);
        }
        
        logger.info("gaterecordMapper.selectByPrimaryKey size: " + gaterecordList.size());
    
        long time1 = System.currentTimeMillis();
        //开启线程
        List<GaterecordVO> gaterecordVOList = dealLeaderListPensionByMutiThread(gaterecordList);
        long time2 = System.currentTimeMillis();
        logger.info("查询时间: [{}]", time2-time1);
        logger.info("多线程获取的记录数 size: [{}]", gaterecordVOList.size());
    
        Collections.sort(gaterecordVOList, new Comparator<GaterecordVO>() {
            @Override
            public int compare(GaterecordVO o1, GaterecordVO o2) {
                int i = o2.getId() - o1.getId();
                return i;
            }
        });
    
        //遍历生成excel
        long time3 = System.currentTimeMillis();
        logger.info("开始生成excel");
        if (gaterecordVOList != null && gaterecordVOList.size() > 0) {
            //1.创建工作簿 HSSFWorkbook 2003   XSSFWorkbook  2007
            Workbook wb = new XSSFWorkbook();
            //2.创建工作表
            Sheet sheet = wb.createSheet();
        
            //设置列宽
            sheet.setColumnWidth(0, 10 * 256);
            sheet.setColumnWidth(1, 20 * 256);
            sheet.setColumnWidth(2, 20 * 256);
            sheet.setColumnWidth(3, 20 * 256);
        
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
            sheet.addMergedRegion(new CellRangeAddress(0, 0, (short) 0, (short) 3));
        
            //横向居中 + 水平居中 + 红色宋体22号
            nCell.setCellStyle(downloadUtil.bigTitleCellStyle(wb));
        
        
            //小标题输出
            //行号rowNo需要变化么 列需要变化么
            rowNo++;
            String[] titles = {"序号", "车主姓名", "车牌号", "时间"};
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
            for (int j = 0; j < gaterecordVOList.size(); j++) {
                GaterecordVO gaterecordVO = gaterecordVOList.get(i);
                cellNo = 0;
                //3 创建行
                nRow = sheet.createRow(rowNo++);
            
                nCell = nRow.createCell(cellNo++);
                nCell.setCellValue(++i + "");
                nCell.setCellStyle(downloadUtil.contentCellStyle(wb));
            
                //4 创建单元格
                //5 设置内容  编号id
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
        long time4 = System.currentTimeMillis();
        logger.info("生成excel时间: [{}]", time4-time3);
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
    
    /***
     * 多线程处理业务
     * @param gaterecordList
     * @return
     * @throws Exception
     */
    public  List<GaterecordVO> dealLeaderListPensionByMutiThread(List<Gaterecord> gaterecordList){
        logger.info("开启多线程前的线程数量: [{}]", Thread.activeCount());
        //一次读取多少条
        int nums = ONCE_READ_COUNT;
        //总记录数
        int count = gaterecordList.size();
        logger.info("count:[{}]", count);
        //线程数量
        int thrednum = (count % nums == 0) ? (count / nums) : (count / nums + 1);
        logger.info("thrednum:[{}]", thrednum);
        List<GaterecordVO> data = new ArrayList<>();
        ExecutorService service = Executors.newFixedThreadPool(thrednum);
        BlockingQueue<Future<List<GaterecordVO>>> queue = new LinkedBlockingQueue<Future<List<GaterecordVO>>>();
        
        int startIndex = 0;
        int maxIndex = 0;
        for (int i = 0; i < thrednum; i++) {
            startIndex = i * nums;
            maxIndex = startIndex + nums;
            if(maxIndex > gaterecordList.size()){
                maxIndex = gaterecordList.size();
            }
            Future<List<GaterecordVO>> future = service.submit(read2List(i, startIndex, maxIndex, gaterecordList));
            queue.add(future);
        }
        
        try {
            int queueSize = queue.size();
            for (int i = 0; i < queueSize; i++) {
                List<GaterecordVO> list = queue.take().get();
                data.addAll(list);
            }
            service.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
            service.shutdown();
        } catch (ExecutionException e) {
            e.printStackTrace();
            service.shutdown();
        }
        return  data;
    }
    private Callable<List<GaterecordVO>> read2List(final int i, final int startIndex, final int maxIndex, List<Gaterecord> gaterecordList) {
        Callable<List<GaterecordVO>> callable = new Callable<List<GaterecordVO>>() {
            public List<GaterecordVO> call() throws Exception {
                List<Gaterecord> subGaterecordList = gaterecordList.subList(startIndex, maxIndex);
                List<GaterecordVO> gaterecordResponses = new ArrayList<>();
                subGaterecordList.forEach(temp ->{
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
                
                return gaterecordResponses;
            }
        };
        return callable;
    }
}
