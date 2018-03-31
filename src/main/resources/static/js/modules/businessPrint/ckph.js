$(function () {
    $("#jqGrid").jqGrid({
        url: baseURL + 'sales/productionorder/list',
        datatype: "json",
        colModel: [
            { label: 'id', name: 'id', index: 'ID', width: 50, key: true ,hidden:true},
            { label: '订单编号', name: 'productionNo', index: 'PRODUCTION_NO', width: 80 },
            { label: '计划单编号', name: 'planNo', index: 'PLAN_NO', width: 80 ,hidden:true},
            { label: '订单类型名称', name: 'orderTypeName', index: 'ORDER_TYPE_NAME', width: 80 },
            { label: '客户编号', name: 'customerNo', index: 'CUSTOMER_NO', width: 80 },
            { label: '客户名称', name: 'customerName', index: 'CUSTOMER_NAME', width: 80 },
            { label: '售点名称', name: 'placeName', index: 'PLACE_NAME', width: 80 },
            { label: '备注', name: 'remakr', index: 'REMAKR', width: 80 },
            { label: '状态', name: 'status', index: 'STATUS', width: 80 ,formatter: function(value, options, row){
                    return value === 0 ?
                        '<span class="label label-danger">禁用</span>' :
                        '<span class="label label-success">正常</span>';
                }
            },
            { label: '创建者', name: 'createUser', index: 'CREATE_USER', width: 80 },
            { label: '创建日期', name: 'createDate', index: 'CREATE_DATE', width: 80 }
        ],
        viewrecords: true,
        height: 385,
        rowNum: 10,
        rowList: [10, 30, 50],
        rownumbers: true,
        rownumWidth: 25,
        autowidth: true,
        multiselect: true,
        pager: "#jqGridPager",
        jsonReader: {
            root: "page.list",
            page: "page.currPage",
            total: "page.totalPage",
            records: "page.totalCount"
        },
        prmNames: {
            page: "page",
            rows: "limit",
            order: "order"
        },
        gridComplete: function () {
            //隐藏grid底部滚动条
            $("#jqGrid").closest(".ui-jqgrid-bdiv").css({"overflow-x": "hidden"});
        },
        subGrid : true,
        subGridRowExpanded : function(subgrid_id,row_id){
            var url = baseURL + 'sales/productionorderdetail/list?productionOrderId='+row_id;
            createSubGrid(subgrid_id,row_id,url);
        }
    });

    function createSubGrid(subgrid_id,row_id,url){
        var subgrid_table_id, pager_id;
        subgrid_table_id = subgrid_id + "_t";
        pager_id = "p_" + subgrid_table_id;
        $("#" + subgrid_id).html("<table id='" + subgrid_table_id + "' class='scroll'></table><div id='" + pager_id + "' class='scroll'></div>");
        jQuery("#" + subgrid_table_id).jqGrid({
            url : url,
            datatype : "json",
            colModel : [
                { label: 'id', name: 'id', index: 'ID', width: 50, key: true ,hidden:true},
                { label: '生产订单ID', name: 'productionOrderId', index: 'PRODUCTION_ORDER_ID', width: 80 ,hidden:true},
                { label: '生产订单编号', name: 'productionOrderNo', index: 'PRODUCTION_ORDER_NO', width: 80 ,hidden:true},
                { label: '产品ID', name: 'prdId', index: 'PRD_ID', width: 80 ,hidden:true},
                { label: '产品编号', name: 'prdNo', index: 'PRD_NO', width: 80 },
                { label: '产品名称', name: 'prdName', index: 'PRD_NAME', width: 80 },
                { label: '产品类型ID', name: 'prdTypeId', index: 'PRD_TYPE_ID', width: 80 ,hidden:true},
                { label: '产品类型名称', name: 'prdTypeName', index: 'PRD_TYPE_NAME', width: 80 },
                { label: '需求数量', name: 'amount', index: 'AMOUNT', width: 80 },
                { label: '定价', name: 'price1', index: 'PRICE1', width: 80 ,hidden:true},
                { label: '售价', name: 'price2', index: 'PRICE2', width: 80 ,hidden:true},
                { label: '成本', name: 'cost', index: 'COST', width: 80 ,hidden:true},
                { label: '预估收入', name: 'revenue', index: 'REVENUE', width: 80 ,hidden:true}
            ],
            rowNum : 20,
            height : '100%',
            rowList : [10,30,50],
            rownumbers: true,
            rownumWidth: 25,
            autowidth:true,
            multiselect: false,
            jsonReader : {
                root: "page.list",
                page: "page.currPage",
                total: "page.totalPage",
                records: "page.totalCount"
            },
            prmNames : {
                page:"page",
                rows:"limit",
                order: "order"
            },
            gridComplete:function(){
                //隐藏grid底部滚动条
                $("#" + subgrid_table_id).closest(".ui-jqgrid-bdiv").css({ "overflow-x" : "hidden" });
            }
        })
    };

    var newDate = new Date();
    var currentDate = newDate.toJSON();

    $(".form_datetime").datetimepicker({
        format: 'yyyy-mm-dd',
        language:'zh-CN',
        autoclose:true,
        minView:2,
        todayBtn:true,
        todayHighlight:true,
        weekStart:1
        // startDate:new Date(currentDate)
    });
    $('.form_datetime').datetimepicker().on('hide', function (ev) {
            var value = $(".form_datetime").val();
            vm.productionOrder.createDate = value;
        });
});

var vm = new Vue({
    el: '#rrapp',
    data: {
        productionOrder: {},
        selectArr:null
    },
    methods: {
        query: function () {
            console.log(JSON.stringify(vm.productionOrder));
            vm.reload();
        },
        initCommbox:function(){
            $.ajax({
                type: "POST",
                url: baseURL + "common/commonUtil/getDataToCommbox",
                // contentType: "application/json",
                data: {tableName:"tb_type_info",search:"PARENT_ID=23",returnField:"ID as value,TYPE_NAME as text"},
                success: function(r){
                    vm.selectArr = r.data;
                    vm.productionOrder.warehouse = 0;
                }
            });
        },
        reload:function(){
            $("#jqGrid").jqGrid('setGridParam',{
                postData:vm.productionOrder
            }).trigger("reloadGrid");
        },
        print:function(){
            var ids = getSelectedRows();
            if(!ids){
                return;
            }
            console.log(ids);
            window.open(baseURL + "businessPrint/print/ckph?token="+token+"&ids="+ids);


        }
    }
});

vm.initCommbox();
