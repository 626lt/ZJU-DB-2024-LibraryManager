主程序启动`mvn exec:java -Dexec.mainClass="Main" -Dexec.cleanupDaemonThreads=false`
草了，这cardtype真傻逼吧
草了，这前端返回的id还是double
除了在数据包里添加action属性，还可以通过url参数传递action种类
草了。。找了半个小时才发现borrow压根没绑定，得在main里面绑定然后重新写一个borrowhandle

el-scrollbar是滚动条
el-table-column是表格列
el-table是表格
el-button是按钮
el-input是输入框
el-dialog是对话框
el-form是表单
el-form-item是表单项
dialog-footer是对话框底部

<el-table :data="books">
                <el-table-column prop="id" label="日期" width="180">
                </el-table-column>
                <el-table-column prop="category" label="姓名" width="180">
                </el-table-column>
                <el-table-column prop="address" label="地址">
                </el-table-column>
            </el-table>

```
.book
|
- 查询按钮，入库按钮，批量入库按钮，还书按钮
|
- 查询结果页面
-- 结果表格，修改图书信息，借书
|
|
```