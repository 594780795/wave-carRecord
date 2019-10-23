package com.wave.carRecord.dao.uts;

import com.wave.carRecord.bean.uts.ViewBpmFata28;
import com.wave.carRecord.common.ViewBpmFata28QueryDTO;

import java.util.List;

public interface ViewBpmFata28Mapper {
    int insert(ViewBpmFata28 record);

    int insertSelective(ViewBpmFata28 record);
    
    ViewBpmFata28 selectByPrimaryKey(Integer id);
    
    List<ViewBpmFata28> selectSelective(ViewBpmFata28QueryDTO utsQueryDTO);
    
}
