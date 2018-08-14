package com.kirin.modules.baseData.controller;

import java.math.BigDecimal;
import java.util.*;

import com.kirin.modules.baseData.entity.BomDetailEntity;
import com.kirin.modules.baseData.entity.MtrDataEntity;
import com.kirin.modules.baseData.service.BomDetailService;
import com.kirin.modules.baseData.service.MtrDataService;
import com.kirin.modules.sys.controller.AbstractController;
import com.kirin.modules.sys.entity.SysUserEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kirin.modules.baseData.entity.BomInfoEntity;
import com.kirin.modules.baseData.service.BomInfoService;
import com.kirin.common.utils.PageUtils;
import com.kirin.common.utils.Query;
import com.kirin.common.utils.R;




/**
 * 
 * 
 * @author kirin
 * @email yang5830404@163.com
 * @date 2017-10-31 16:31:30
 */
@RestController
@RequestMapping("/baseData/bominfo")
public class BomInfoController extends AbstractController {
	@Autowired
	private BomInfoService bomInfoService;

	@Autowired
	private BomDetailService bomDetailService;
	
	/**
	 * 列表
	 */
	@RequestMapping("/list")
	@RequiresPermissions("baseData:bominfo:list")
	public R list(@RequestParam Map<String, Object> params){
		//查询列表数据
        Query query = new Query(params);

		List<BomInfoEntity> bomInfoList = bomInfoService.queryList(query);
		int total = bomInfoService.queryTotal(query);
		
		PageUtils pageUtil = new PageUtils(bomInfoList, total, query.getLimit(), query.getPage());
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@RequestMapping("/info/{id}")
	@RequiresPermissions("baseData:bominfo:info")
	public R info(@PathVariable("id") Long id){
		BomInfoEntity bomInfo = bomInfoService.queryObject(id);
		
		return R.ok().put("bomInfo", bomInfo);
	}

	/**
	 * 通过原料查找配方信息
	 */
	@RequestMapping("/listByMtrId")
//	@RequiresPermissions("baseData:bominfo:info")
	public R listbyMtrId(@RequestParam Map<String, Object> params){
		//查询列表数据
		Query query = new Query(params);

		List<BomInfoEntity> bomInfoList = bomInfoService.queryObjectByMtrId(Long.valueOf(String.valueOf(query.get("mtrId"))).longValue());
		int total = bomInfoService.queryTotal(query);

		PageUtils pageUtil = new PageUtils(bomInfoList, total, query.getLimit(), query.getPage());

		return R.ok().put("page", pageUtil);
	}


	/**
	 * 保存
	 */
	@RequestMapping("/save")
	@RequiresPermissions("baseData:bominfo:save")
	public R save(@RequestBody BomInfoEntity bomInfo){
		SysUserEntity sysUserEntity =  getUser();

		bomInfo.setCreateUser(sysUserEntity.getUsername());
		bomInfo.setCreateDate(new Date());





//		判断配方是否重复（一个产品只能关联一个配方）
		BomInfoEntity entity = bomInfoService.queryObjectByPrdId(bomInfo.getPrdId());
		if(entity != null) {
			return R.error("一个产品只能关联一个配方");
		}

		bomInfoService.save(bomInfo);
		
		return R.ok();
	}


	/**
	 * 复制
	 */
	@RequestMapping("/copysave/{id}")
	@RequiresPermissions("baseData:bominfo:save")
	public R copysave(@RequestBody BomInfoEntity bomInfo, @PathVariable("id") String oldbomid) {
		SysUserEntity sysUserEntity = getUser();

		bomInfo.setCreateUser(sysUserEntity.getUsername());
		bomInfo.setCreateDate(new Date());

//		判断配方是否重复（一个产品只能关联一个配方）
		BomInfoEntity entity = bomInfoService.queryObjectByPrdId(bomInfo.getPrdId());
		if (entity != null) {
			return R.error("一个产品只能关联一个配方");
		}

		//保存配方
		BomInfoEntity oldbomInfo = bomInfoService.queryObject(Long.valueOf(oldbomid));
		bomInfo.setCost(oldbomInfo.getCost());
		bomInfo.setSumModiWgt(oldbomInfo.getSumModiWgt());
		bomInfo.setSumCostRate(oldbomInfo.getSumCostRate());
		bomInfo.setSumGrossWgt(oldbomInfo.getSumGrossWgt());
		bomInfo.setSumNetWgt(oldbomInfo.getSumNetWgt());
		bomInfo.setPotWgt(oldbomInfo.getPotWgt());
		bomInfo.setBoxWgt(oldbomInfo.getBoxWgt());
		bomInfo.setPrice(oldbomInfo.getPrice());
		bomInfoService.save(bomInfo);

		//复制配方
		Map<String, Object> params = new HashMap<>();
		params.put("bomInfoId", oldbomid);
		params.put("page", "1");
		params.put("limit", "999999");
		Query query = new Query(params);
		List<BomDetailEntity> bomDetailList = bomDetailService.queryList(query);
		System.out.println(bomDetailList.size());
		Iterator<BomDetailEntity> iterator = bomDetailList.iterator();
		while (iterator.hasNext()) {
			BomDetailEntity current = iterator.next();
			current.setId(null);
			current.setBomId(bomInfo.getId());
			current.setCreateUser(bomInfo.getCreateUser());
			current.setCreateDate(bomInfo.getCreateDate());
			current.setUpdateUser(null);
			current.setUpdateDate(null);
			bomDetailService.save(current);
		}

		BomInfoEntity newBomInfo = bomInfoService.queryObjectByPrdId(bomInfo.getPrdId());

		return R.ok().put("bomId", newBomInfo.getId()).put("bomname", newBomInfo.getBomName());
	}


	/**
	 * 修改
	 */
	@RequestMapping("/update")
	@RequiresPermissions("baseData:bominfo:update")
	public R update(@RequestBody BomInfoEntity bomInfo){
		SysUserEntity sysUserEntity =  getUser();

		bomInfo.setUpdateUser(sysUserEntity.getUsername());
		bomInfo.setUpdateDate(new Date());

		bomInfoService.update(bomInfo);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@RequestMapping("/delete")
	@RequiresPermissions("baseData:bominfo:delete")
	public R delete(@RequestBody Long[] ids){
		bomInfoService.deleteBatch(ids);
		
		return R.ok();
	}

	@RequestMapping("/detailConfig")
	@RequiresPermissions("baseData:bominfo:detailConfig")
	public R detailConfig(){
		return R.ok();
	}





}
