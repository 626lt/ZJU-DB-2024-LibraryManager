<template>
    <el-scrollbar height="100%" style="width: 100%;">
        <!-- 标题和搜索框 -->
        <div style="margin-top: 20px; margin-left: 40px; font-size: 2em; font-weight: bold; ">图书管理
        </div>

       
        <div style="width:30%;margin:10px ; padding-top:3vh;">
             <!-- 图书入库按钮 -->
            <el-button style="margin-left: 20px;" type="primary" @click="newBookInfo.Category='',
            newBookInfo.Title='',newBookInfo.Press='',newBookInfo.Publishyear='',newBookInfo.Author='',newBookInfo.Price='',newBookInfo.Number=''
            ;newBookVisible = true">图书入库
            </el-button>
            <!-- 批量入库按钮 -->
            <el-button style="margin-left: 20px;" type="primary" @click="newBookInfo.Category='',
            newBookInfo.Title='',newBookInfo.Press='',newBookInfo.Publishyear='',newBookInfo.Author='',newBookInfo.Price='',newBookInfo.Number=''
            ;newBookVisible = true">批量入库
            </el-button>
        </div>
        


       <!-- 图书入库对话框 -->
        <el-dialog v-model="newBookVisible" title="图书入库" width="30%">
            <el-form :model="newBookInfo" label-width="80px">
                <el-form-item label="类别">
                    <el-input v-model="newBookInfo.Category"></el-input>
                </el-form-item>
                <el-form-item label="书名">
                    <el-input v-model="newBookInfo.Title"></el-input>
                </el-form-item>
                <el-form-item label="出版社">
                    <el-input v-model="newBookInfo.Press"></el-input>
                </el-form-item>
                <el-form-item label="出版年份">
                    <el-input v-model="newBookInfo.Publishyear"></el-input>
                </el-form-item>
                <el-form-item label="作者">
                    <el-input v-model="newBookInfo.Author"></el-input>
                </el-form-item>
                <el-form-item label="价格">
                    <el-input v-model="newBookInfo.Price"></el-input>
                </el-form-item>
                <el-form-item label="数量">
                    <el-input v-model="newBookInfo.Number"></el-input>
                </el-form-item>
            </el-form>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="newBookVisible = false">取消</el-button>
                    <el-button type="primary" @click="ConfirmNewBook">确定</el-button>
                </span>
            </template>

        </el-dialog>

        <!-- 图书查询按钮 -->
        <div style="width:30%;margin:10px ; padding-top:3vh;">
            <el-button style="margin-left: 20px;" type="primary" @click="queryBookInfo.Category='',
            queryBookInfo.Title='',queryBookInfo.Press='',queryBookInfo.MinPublishyear='',
            queryBookInfo.MaxPublishyear='',queryBookInfo.Author='',queryBookInfo.MinPrice='',
            queryBookInfo.MaxPrice='',queryBookVisible=true">
            </el-button>
            <el-button style="margin-left: 20px;" type="primary" >还书</el-button>
        </div>

        <!-- 图书查询对话框 -->  
        <el-dialog v-model="queryBookVisible" title="图书查询" width="30%">
            <el-form :model="queryBookInfo" label-width="100px">
                <el-form-item label="类别">
                    <el-input v-model="queryBookInfo.Category"></el-input>
                </el-form-item>
                <el-form-item label="书名">
                    <el-input v-model="queryBookInfo.Title"></el-input>
                </el-form-item>
                <el-form-item label="出版社">
                    <el-input v-model="queryBookInfo.Press"></el-input>
                </el-form-item>
                <el-form-item label="最小出版年份">
                    <el-input v-model="queryBookInfo.MinPublishyear"></el-input>
                </el-form-item>
                <el-form-item label="最大出版年份">
                    <el-input v-model="queryBookInfo.MaxPublishyear"></el-input>
                </el-form-item>
                <el-form-item label="作者">
                    <el-input v-model="queryBookInfo.Author"></el-input>
                </el-form-item>
                <el-form-item label="最小价格">
                    <el-input v-model="queryBookInfo.MinPrice"></el-input>
                </el-form-item>
                <el-form-item label="最大价格">
                    <el-input v-model="queryBookInfo.MaxPrice"></el-input>
                </el-form-item>
            </el-form>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="queryBookVisible = false">取消</el-button>
                    <el-button type="primary" @click="QueryBook">确定</el-button>
                </span>
            </template>
        </el-dialog>
        
        <!-- 修改图书对话框 -->  
        <el-dialog v-model="modifyBookVisible" title="修改信息" width="30%">
            <el-form :model="modifyBookInfo" label-width="100px">
                <el-form-item label="类别">
                    <el-input v-model="modifyBookInfo.Category"></el-input>
                </el-form-item>
                <el-form-item label="书名">
                    <el-input v-model="modifyBookInfo.Title"></el-input>
                </el-form-item>
                <el-form-item label="出版社">
                    <el-input v-model="modifyBookInfo.Press"></el-input>
                </el-form-item>
                <el-form-item label="出版年份">
                    <el-input v-model="modifyBookInfo.Publishyear"></el-input>
                </el-form-item>
                <el-form-item label="作者">
                    <el-input v-model="modifyBookInfo.Author"></el-input>
                </el-form-item>
                <el-form-item label="价格">
                    <el-input v-model="modifyBookInfo.Price"></el-input>
                </el-form-item>
                <el-form-item label="库存">
                    <el-input v-model="modifyBookInfo.Stock"></el-input>
                </el-form-item>
            </el-form>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="modifyBookVisible = false">取消</el-button>
                    <el-button type="primary" @click="QueryBook">确定</el-button>
                </span>
            </template>
        </el-dialog>
        
        <!-- 图书查询结果显示 -->
        <el-table :data="books" border style="width: 100%">
            <el-table-column prop="id" label="编号" width="80"/>
            <el-table-column prop="category" label="类别" width="100"/>
            <el-table-column prop="title" label="书名" width="150"/>
            <el-table-column prop="press" label="出版社" width="150"/>
            <el-table-column prop="publishyear" label="出版年份" width="150"/>
            <el-table-column prop="author" label="作者" width="150"/>
            <el-table-column prop="price" label="价格" width="150"/>
            <el-table-column prop="stock" label="库存" width="145"/>
            <el-table-column fixed="right" label="Operations" width="200">
            <template #default >
                <el-button type="primary" @click="modifyBook(row)">修改信息</el-button>
                <el-button>借书</el-button>
            </template>
            </el-table-column>

        </el-table>
  
    </el-scrollbar>




</template>



<script>
import { Delete, Edit, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export default {
    data() {
        return {
            Delete,
            Edit,
            Search,
            toSearch: '', // 搜索内容
            books: [], // 图书列表
            newBookVisible: false, // 新建图书对话框可见性
            newCardVisible: false, // 新建借书证对话框可见性
            removeCardVisible: false, // 删除借书证对话框可见性
            toRemove: 0, // 待删除借书证号
            newBookInfo: { // 待新建图书信息
                Category: '',
                Title: '',
                Press: '',
                Publishyear: '',
                Author: '',
                Price: '',
                Number: ''
            },
            queryBookVisible: false, // 查询图书对话框可见性
            queryBookInfo: { // 待查询图书信息
                Category: '',
                Title: '',
                Press: '',
                MinPublishyear: '',
                MaxPublishyear: '',
                Author: '',
                MinPrice: '',
                MaxPrice: ''
            },
            modifyBookVisible: false, // 修改信息对话框可见性
            modifyBookInfo: null, // 待修改图书信息
            selectedBook: null // 选中的图书

        }
    },
    methods: {
        ConfirmNewBook() {
            // 发出POST请求
            axios.post("/book/",
                { // 请求体
                    action: "newbook",
                    Category: this.newBookInfo.Category,
                    Title: this.newBookInfo.Title,
                    Press: this.newBookInfo.Press,
                    Publishyear: this.newBookInfo.Publishyear,
                    Author: this.newBookInfo.Author,
                    Price : this.newBookInfo.Price,
                    Number : this.newBookInfo.Number
                })
                .then(response => {
                    ElMessage.success("图书入库成功") // 显示消息提醒
                    this.newBookVisible = false // 将对话框设置为不可见
                })
                .catch(error => {
                    ElMessage.error("图书入库失败") // 显示消息提醒
                })
        },
        ConfirmModifyCard() {
            // 发出POST请求
            axios.post("/card/",
                { // 请求体
                    action: "modifycard",
                    id: this.toModifyInfo.id,
                    name: this.toModifyInfo.name,
                    department: this.toModifyInfo.department,
                    type: this.toModifyInfo.type
                })
                .then(response => {
                    ElMessage.success("借书证信息修改成功") // 显示消息提醒
                    this.modifyCardVisible = false // 将对话框设置为不可见
                    this.QueryCards() // 重新查询借书证以刷新页面
                })
                .catch(error => {
                    ElMessage.error("借书证信息修改失败") // 显示消息提醒
                })
        },
        ConfirmRemoveCard() {
            // 发出DELETE请求
            axios.post("/card/", 
                {
                    action: "deletecard",
                    id:this.toRemove
                })
                .then(response => {
                    ElMessage.success("借书证删除成功") // 显示消息提醒
                    this.removeCardVisible = false // 将对话框设置为不可见
                    this.QueryCards() // 重新查询借书证以刷新页面
                })
                .catch(error => {
                    ElMessage.error("借书证删除失败，存在未还书籍！") // 显示消息提醒
                })
        },
        QueryBook() {
            this.books = [] // 清空列表
            let response = axios.get('/book',{ params: {
                    Category:this.Category,
                    Title:this.Title,
                    Press:this.Press,
                    MinPublishyear:this.MinPublishyear,
                    MaxPublishyear:this.MaxPublishyear,
                    Author:this.Author,
                    MinPrice:this.MinPrice,
                    MaxPrice:this.MaxPrice, 
                } }) // 向/card发出GET请求
                .then(response => {
                    let books = response.data // 接收响应负载
                    //ElMessage.success("接受成功")
                    books.forEach(book => { // 对于每书
                        this.books.push(book) // 将其加入到列表中
                    })
                    ElMessage.success("查询成功") // 显示消息提醒
                    this.queryBookVisible = false // 将对话框设置为不可见
                })
        },
        modifyBook(row) {
            this.modifyBookVisible = true
            this.modifyBookInfo = row
        }
    },
    // mounted() {
    //     this.QueryBook() // 查询借书证以刷新页面
    // }
}   

</script>


<style scoped>
.cardBox {
    height: 300px;
    width: 200px;
    box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
    text-align: center;
    margin-top: 40px;
    margin-left: 27.5px;
    margin-right: 10px;
    padding: 7.5px;
    padding-right: 10px;
    padding-top: 15px;
}

.newCardBox {
    height: 300px;
    width: 200px;
    margin-top: 40px;
    margin-left: 27.5px;
    margin-right: 10px;
    padding: 7.5px;
    padding-right: 10px;
    padding-top: 15px;
    box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
    text-align: center;
}

.scrollbar-demo-item {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 50px;
    margin: 10px;
    text-align: center;
    border-radius: 4px;
    background: var(--el-color-primary-light-9);
    color: var(--el-color-primary);
}
</style>
