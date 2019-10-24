var vm = new Vue({
    el:'#rrapp',
    data:{
        q:{
            plate: '',
            validCode : '',
        },
        exportType: null,
        dateSelect: {
            startDate: null,
            endDate: null,
            total: 0
        },
        isLoad: false,
        title:null,
        datePickerDefault:null
    },
    methods: {
        query: function () {
            if (vm.q.validCode == null || vm.q.validCode == '') {
                //提示层
                layer.msg('请输入密码', {
                    icon: 7,
                    time: 2000 //2秒关闭（如果不配置，默认是3秒）
                });
                return;
            }
            vm.isLoad = true; 
            vm.reload();
        },
        reload: function () {
            this.$Loading.start();
            $('#jqGrid').jqGrid('setGridParam', {url: baseURL + 'car/queryCarRecord'}).trigger('reloadGrid');
            this.$Loading.finish();
        },
        clear: function () {
            vm.dateSelect.startDate = null,
            vm.dateSelect.endDate = null, 
            vm.datePickerDefault = null,
            vm.q.plate = ''
        },
        changeDate: function (daterange) {
            if (daterange != null) {
                vm.dateSelect.startDate = daterange[0];
                vm.dateSelect.endDate = daterange[1];
                $("#jqGrid").jqGrid("setGridParam",{page:1}).trigger("reloadGrid");
            }
        },
        export: function(exportType){
            console.log(Date());
            var oReq = new XMLHttpRequest();
            oReq.open("POST", baseURL + "car/excel", true);
            oReq.responseType = "blob";
            oReq.setRequestHeader('content-type', 'application/json');
            oReq.onload = function (oEvent) {
                var content = oReq.response;
                var elink = document.createElement('a');
                elink.download = '车辆进出记录'+'.xlsx';
                elink.style.display = 'none';

                var blob = new Blob([content]);
                elink.href = URL.createObjectURL(blob);

                document.body.appendChild(elink);
                elink.click();

                document.body.removeChild(elink);

                console.log(Date());
            };
            oReq.send(
                JSON.stringify({
                    'search': null,
                    "nd": null,
                    "sidx": null,
                    "pageNumber": $("#jqGrid").jqGrid('getGridParam','page'),
                    "pageSize": $("#jqGrid").jqGrid('getGridParam','rowNum'),
                    "plate": vm.q.plate == null || vm.q.plate == ''? null : vm.q.plate,
                    "startDate": vm.dateSelect.startDate,
                    "endDate": vm.dateSelect.endDate,
                    "exportType": exportType,
                    "validCode": vm.q.validCode
                }));
        },
        exportAllExcel: function(exportType) {
            if (vm.dateSelect.total == 0) {
                //提示层
                layer.msg('无数据可以导出', {
                    icon: 7,
                    time: 2000 //2秒关闭（如果不配置，默认是3秒）
                });
                return;
            }
            if (vm.q.validCode == null || vm.q.validCode == '') {
                //提示层
                layer.msg('请输入密码', {
                    icon: 7,
                    time: 2000 //2秒关闭（如果不配置，默认是3秒）
                });
                return;
            }
            var content = '';
            if (exportType == 1) {
                if (vm.dateSelect.startDate == null || vm.dateSelect.startDate == '' || vm.dateSelect.endDate == '' || vm.dateSelect.endDate == '') {
                    content = '即将导出' + vm.dateSelect.total + '条记录，可能耗时1到2分钟。点击确认后，开始在后台生成表格，请耐心等待下载框弹出';
                } else {
                    content = '即将导出' + vm.dateSelect.total + '条记录。点击确认后，开始在后台生成表格，请耐心等待下载框弹出';;
                }
            } else {
                content = '即将导出' + $("#jqGrid").jqGrid('getGridParam','rowNum') + '条记录。点击确认后，开始在后台生成表格，请耐心等待下载框弹出';;
            }
            
            //导出全部
            layer.open({
                type: 0,
                title:'导出确认',
                area: [300 , 300],
                fixed: false, //不固定
                maxmin: true,
                content: content,
                btn: ['确定', '取消'], //按钮组
                scrollbar: false ,//屏蔽浏览器滚动条
                yes: function(index){//layer.msg('yes');    //点击确定回调
                    layer.close(index);
                    vm.export(exportType);
                },
                btn2: function(){//layer.alert('aaa',{title:'msg title'});  ////点击取消回调
                    layer.close();
                }
            });
        },
    },
});

$("#jqGrid").jqGrid({
    // url: baseURL + 'car/queryCarRecord',
    url: '',
    datatype: "json",
    mtype: "POST",
    ajaxGridOptions: { contentType: 'application/json;charset=utf-8'},
    colModel: [
        { label: '记录ID', name: 'id', index: "id", width: 45, key: true, align: "center", sortable: false},
        // { label: '设备', name: 'device', index: "device", width: 75, sortable: false , hidden: true},
        { label: '车主姓名', name: 'name', index: "name", sortable: false, width: 75 , sortable: false , align: "center"},
        { label: '车牌号', name: 'plate', index: "plate", width: 90 , sortable: false, align: "center" },
        // { label: '出入口', name: 'type', index: "type", width: 100 , sortable: false, align: "center" },
        // { label: '授权方式', name: 'authentication', index: "authentication", width: 100, sortable: false  , align: "center"},
        // { label: 'authentication', name: 'status', width: 60, formatter: function(value, options, row){
        // 	return value === 0 ? 
        // 		'<span class="label label-danger">禁用</span>' : 
        // 		'<span class="label label-success">正常</span>';
        // }},
        { label: '时间', name: 'timestamp', index: "timestamp", sortable: false , align: "center"}
    ],
    viewrecords: true,
    height: 555,
    rowNum: 15,
    rowList : [15,30,50],
    rownumbers: true,
    rownumWidth: 25,
    autowidth: true,
    multiselect: false,
    pager: "#jqGridPager",
    loadui: "block",
    jsonReader : {
        total: "data.totalPage",
        root: "data.gaterecordVOList",
        page: "data.currPage",
        records: "data.currCount"
    },
    gridComplete:function(){
        //隐藏grid底部滚动条
        $("#jqGrid").closest(".ui-jqgrid-bdiv").css({ "overflow-x" : "hidden" });
    },
    loadComplete: function (data) {
        if (data.code == 0 ) {
            vm.dateSelect.total = data.data.total
        } else if (vm.isLoad){
            //提示层
            layer.msg(data.msg, {
                icon: 2,
                time: 2000 //2秒关闭（如果不配置，默认是3秒）
            });
        }
    },
    beforeRequest: function(){
        var page = $("#jqGrid").jqGrid('getGridParam','page');
        var data = {
            'search': null,
            "nd": null,
            "sidx": null,
            "pageNumber": $("#jqGrid").jqGrid('getGridParam','page'),
            "pageSize": $("#jqGrid").jqGrid('getGridParam','rowNum'),
            "plate": vm.q.plate == null || vm.q.plate == ''? null : vm.q.plate,
            "startDate": vm.dateSelect.startDate,
            "endDate": vm.dateSelect.endDate,
            "validCode": vm.q.validCode
        };

        $("#jqGrid").jqGrid('setGridParam',{
            postData: JSON.stringify(data),
        })
    }
});

$(function () {

    //ie刷新不清空输入框的值
    $('#plate').val('');
    
    //默认查询最近一个月
    vm.datePickerDefault = [getLastMonthFormatDate(), getCurrentFormatDate()];
    vm.dateSelect.startDate = getLastMonthFormatDate();
    vm.dateSelect.endDate = getCurrentFormatDate();
});

/**
 * 获取上个月的日期 yyyy-MM-dd
 * @returns {string}
 */
function getLastMonthFormatDate(){
    var nowDate = new Date();
    var year = nowDate.getFullYear();
    var month = nowDate.getMonth();
    //当前月的总天数
    var currentDay = nowDate.getDate();
    var yearStr = year;
    var monthStr = month;
    var dateStr = '';
    if (nowDate.getMonth() == 0) {
        yearStr = parseInt(year) - 1;
        monthStr = 12;
    } else {
        monthStr = nowDate.getMonth() < 10 ? "0" + (nowDate.getMonth()) : nowDate.getMonth();
    }
    var lastDate = new Date(nowDate.getFullYear(), parseInt(month), 0);
    var lastDay = new Date(lastDate).getDate();
    if (lastDay < parseInt(currentDay)) {//上月总天数<本月日期，比如3月的30日，在2月中没有30
        dateStr = lastDay;
    } else {
        dateStr = nowDate.getDate() < 10 ? "0" + nowDate.getDate() : nowDate.getDate();
    }
    
    // var hour = nowDate.getHours()< 10 ? "0" + nowDate.getHours() : nowDate.getHours();    
    // var minute = nowDate.getMinutes()< 10 ? "0" + nowDate.getMinutes() : nowDate.getMinutes();    
    // var second = nowDate.getSeconds()< 10 ? "0" + nowDate.getSeconds() : nowDate.getSeconds();  

    return yearStr + "-" + monthStr + "-" + dateStr;
}

/**
 * 获取当前日期 yyyy-MM-dd
 * @returns {string}
 */
function getCurrentFormatDate(){
    var nowDate = new Date();
    var year = nowDate.getFullYear();
    var month = nowDate.getMonth() + 1  < 10 ? "0" + (nowDate.getMonth() + 1) : nowDate.getMonth() + 1;
    var date = nowDate.getDate() < 10 ? "0" + nowDate.getDate() : nowDate.getDate();
    // var hour = nowDate.getHours()< 10 ? "0" + nowDate.getHours() : nowDate.getHours();    
    // var minute = nowDate.getMinutes()< 10 ? "0" + nowDate.getMinutes() : nowDate.getMinutes();    
    // var second = nowDate.getSeconds()< 10 ? "0" + nowDate.getSeconds() : nowDate.getSeconds();
    return year + "-" + month + "-" + date;
}  
