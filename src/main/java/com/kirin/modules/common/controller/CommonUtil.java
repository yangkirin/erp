package com.kirin.modules.common.controller;

import com.kirin.common.utils.PageUtils;
import com.kirin.common.utils.Query;
import com.kirin.common.utils.R;
import com.kirin.modules.baseData.entity.TypeInfoEntity;
import com.kirin.modules.baseData.service.TypeInfoService;
import com.kirin.modules.common.service.CommonUtilService;
import com.kirin.modules.common.utils.CommonUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kirin.common.utils.PinyinUtil;

@RestController
@RequestMapping("/common/commonUtil")
public class CommonUtil {

    @Autowired
    private CommonUtilService commonUtilService;

    @Autowired
    private TypeInfoService typeInfoService;

    @RequestMapping("/word2pinying/{word}")
    public R pinyingCode(@PathVariable("word") String word){
        String code = PinyinUtil.getPinYinHeadUperChar(word);
        return R.ok().put("pinying", code);
    }

    @RequestMapping("/getCommbox")
    public R getCommbox(@RequestParam("tableName") String tableName,@RequestParam("returnField") String returnField,@RequestParam("searchCond") String searchId){
        String[] fieldNameArr = returnField.split(":");
        String[] searchArr = searchId.split(":");

        commonUtilService.getCommbox(tableName,fieldNameArr,searchArr);

        return R.ok().put("dataJson","111");
    }


    @RequestMapping("/getTableMaxId")
    public R getTableMaxId(@RequestParam("tableName") String tableName,@RequestParam("id") String id){
        Integer maxId = commonUtilService.getTableMaxId(tableName,id);
        String idStr = String.format("%05d",maxId);
        return R.ok().put("maxNo",idStr);
    }


    @RequestMapping("/createNewNo")
    public R createNewNo(@RequestParam("tableName") String tableName,@RequestParam("id") String id,@RequestParam("typeInfoId") String typeInfoId){
        //1.一层No
        Integer maxId = commonUtilService.getTableMaxId(tableName,id);
        String idStr = String.format("%05d",maxId);


        TypeInfoEntity typeInfoEntity = typeInfoService.queryObject(Long.valueOf(typeInfoId));
        idStr = String.format(typeInfoEntity.getTypeCode()+"%s",idStr);

        while (typeInfoEntity.getParentId() != 0){
            typeInfoEntity = typeInfoService.queryObject(typeInfoEntity.getParentId());
            idStr = String.format(typeInfoEntity.getTypeCode()+"%s",idStr);
        }
        return R.ok().put("newNo",idStr);
    }

    @RequestMapping("/getFieldData")
    public R getFieldData(@RequestParam("tableName")String tableName,@RequestParam("returnField")String returnField,@RequestParam("fieldName")String fieldName,@RequestParam("searchWord")String searchWord){
        String[] fieldNameArr = fieldName.split(":");
        List<String> data_arr = commonUtilService.getFieldData(tableName,returnField,fieldNameArr,searchWord);
        return R.ok().put("data",data_arr);
    }

    @RequestMapping("/getTableData")
    public R getTableData(@RequestParam("tableName")String tableName,@RequestParam("searchWord")String searchWord,@RequestParam("fieldName")String fieldName){
        String[] fieldNameArr = fieldName.split(":");
        List<Map> data_arr = commonUtilService.getTableData(tableName,searchWord,fieldNameArr);
        return R.ok().put("data",data_arr);
    }

    @RequestMapping("/getTableNewNo")
    public R getTableNewNo(@RequestParam("tableName")String tableName,@RequestParam("noField")String noField,@RequestParam(value="length",required = false)Long length){
        String newNo = "";
        Long no = 0L;
        String currentNo = commonUtilService.getTableNewNo(tableName,noField);
        if(length != null && length > 0){
            if(currentNo != null && !currentNo.equals("")){
                no = Long.valueOf(currentNo);
                no = Long.valueOf(currentNo);
                no = no+1;
                newNo = String.format("%0"+length+"d",no);
            }else{
                newNo = String.format("%0"+length+"d",1);
            }
        }else{
            if(currentNo == null || currentNo.equals("")){
                currentNo = "000";
            }
            int len = currentNo.length();
            if(currentNo != null && !currentNo.equals("")){
                if(currentNo.length() >= 8){
                    //yyyyMMdd+0000
                    no = Long.valueOf(currentNo.substring(8));
                    newNo = new CommonUtils().createNoByDate(no+1);
                }else{
                    no = Long.valueOf(currentNo);
                    no = no+1;
                    newNo = String.format("%0"+len+"d",no);
                }
            }
        }
        return R.ok().put("newNo",newNo);
    }

    public String createDateNewNo(String tableName,String noField){

        return "";
    }


    public String getTableNewNoStr(String tableName,String noField){
        Long no = 0L;
        String currentNo = commonUtilService.getTableNewNo(tableName,noField);
        if(currentNo != null && !currentNo.equals("")){
            //yyyyMMdd+0000
            no = Long.valueOf(currentNo.substring(8));
        }
        no = no+1;
        return new CommonUtils().createNoByDate(no);
    }

    @RequestMapping("/getDataToCommbox")
    public R getDataToCommbox(@RequestParam("tableName")String tableaName,@RequestParam("search")String search,@RequestParam("returnField")String returnField){
        List<Map> map = commonUtilService.getDataToCommbox(tableaName,search,returnField);

        Map<String,String> map_empty = new HashMap<>();
        map_empty.put("value","0");
        map_empty.put("text","空");

        map.add(map_empty);
        return R.ok().put("data",map);
    }


    public static void main(String[] args){
        Integer id  = 3234;
        String str = String.format("%05d",id);
        str = String.format("01"+"%s",id);

        System.out.println(str);

        String srt_a = "a:b";
        System.out.println(srt_a.split(":").length);
        String srt_b = "a";
        System.out.println(srt_b.split(":").length);

        String s = "201711140000";
        System.out.println(s.substring(8));
    }
}