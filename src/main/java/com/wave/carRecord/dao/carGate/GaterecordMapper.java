package com.wave.carRecord.dao.carGate;

import com.wave.carRecord.bean.carGate.Gaterecord;
import com.wave.carRecord.common.GaterecordQueryDTO;

import java.util.List;

public interface GaterecordMapper{
    int deleteByPrimaryKey(Integer id);

    int insert(Gaterecord record);
    
    List<Gaterecord> selectSelective(GaterecordQueryDTO gaterecordDTO);
    
    /**
     * 根据条件分页查询
     * @param gaterecordDTO
     * @return
     */
    List<Gaterecord> selectPageSelective(GaterecordQueryDTO gaterecordDTO);

    int insertSelective(Gaterecord record);

    Gaterecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Gaterecord record);

    int updateByPrimaryKey(Gaterecord record);
}
