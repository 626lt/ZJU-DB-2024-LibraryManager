<template>
    <el-scrollbar height="100%" style="width: 100%;">
        <!-- 标题和搜索框 -->
        <div style="margin-top: 20px; margin-left: 40px; font-size: 2em; font-weight: bold; ">图书管理
            <el-input v-model="toSearch" :prefix-icon="Search"
                style=" width: 15vw;min-width: 150px; margin-left: 30px; margin-right: 30px; float: right;" clearable />
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
        <div style="width:50%;margin:10px ; padding-top:3vh;">
            <el-button style="margin-left: 20px;" type="primary" @click="newBookInfo.Category='',
            newBookInfo.Title='',newBookInfo.Press='',newBookInfo.Publishyear='',newBookInfo.Author='',newBookInfo.Price='',newBookInfo.Number=''
            ;newBookVisible = true">图书入库
            </el-button>
            <el-button style="margin-left: 20px;" type="primary" @click="queryBookInfo.Category='',
            queryBookInfo.Title='',queryBookInfo.Press='',queryBookInfo.MinPublishyear='',
            queryBookInfo.MaxPublishyear='',queryBookInfo.Author='',queryBookInfo.MinPrice='',
            queryBookInfo.MaxPrice='',queryBookVisible=true">图书查询
            </el-button>
            <el-button style="margin-left: 20px;" type="primary" @click="returnBookVisible =true" >还书</el-button>     
        </div>
        
        
        <el-dialog v-model="uploadVisible" title="上传文件" width="30%">
            <el-upload
                class="upload-demo"
                drag
                action="http://localhost:8000/upload"
                :on-preview="handlePreview"
                :on-remove="handleRemove"
                :file-list="fileList"
                :auto-upload="true"
                multiple>
                <i class="el-icon-upload"></i>
                <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
                <div class="el-upload__tip" slot="tip">只能上传json文件，且不超过500kb</div>
            </el-upload>
    <span slot="footer" class="dialog-footer">
        <el-button @click="uploadVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitUpload">确 定</el-button>
    </span>
</el-dialog>


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
        
         <!-- 图书卡片显示区 -->
        <div style="display: flex;flex-wrap: wrap; justify-content: start;">
            <!-- 图书卡片 -->
            <div class="bookBox" v-for="book in books" v-show="book.category.includes(toSearch)" :key="book.id">
                <div>
                    <!-- 卡片标题 -->
                    <div style="font-size: 25px; font-weight: bold;">No. {{ book.id }}</div>
                    <el-divider />

                    <!-- 卡片内容 -->
                    <div style="margin-left: 10px; text-align: start; font-size: 16px;">
                        <p style="padding: 2.5px;"><span style="font-weight: bold;">类型：</span>{{ book.category }}</p>
                        <p style="padding: 2.5px;"><span style="font-weight: bold;">书名：</span>{{ book.title }}</p>
                        <p style="padding: 2.5px;"><span style="font-weight: bold;">出版社：</span>{{ book.press }}</p>
                        <p style="padding: 2.5px;"><span style="font-weight: bold;">出版年份：</span>{{ book.publishyear }}</p>
                        <p style="padding: 2.5px;"><span style="font-weight: bold;">作者：</span>{{ book.author }}</p>
                        <p style="padding: 2.5px;"><span style="font-weight: bold;">价格：</span>{{ book.price }}</p>
                        <p style="padding: 2.5px;"><span style="font-weight: bold;">库存：</span>{{ book.stock }}</p>
                    </div>

                    <el-divider />

                    <!-- 卡片操作 -->
                    <div style="margin-top: 5px;">
                        <el-button type="primary" :icon="Edit" @click="
                        modifyBookInfo.id=book.id,
                        modifyBookInfo.Category=book.category,
                        modifyBookInfo.Title=book.title,
                        modifyBookInfo.Press=book.press,
                        modifyBookInfo.Publishyear=book.publishyear,
                        modifyBookInfo.Author=book.author,
                        modifyBookInfo.Price=book.price,
                        modifyBookInfo.stock=book.stock,
                        modifyBookVisible = true" circle />
                        <el-button type="danger" :icon="Delete" circle
                            @click="this.toRemove = book.id, this.removeBookVisible = true"
                             />
                        <el-button type="success" :icon="Check" circle
                            @click="this.selectedBook.id = book.id, this.selectedBook.deltastock=0,this.incstockVisible = true"
                        />
                        <el-button type="warning" :icon="Star" circle
                            @click="this.borrow.id = book.id, this.borrowVisible = true"
                        />
                    </div>

                </div>
            </div>
        </div>


        <!-- 还书对话框 -->
        <el-dialog v-model="returnBookVisible" title="还书" width="30%">
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                编号
                <el-input v-model.number="returnBookInfo.id" style="width: 12.5vw;" clearable />
            </div>
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                借书证编号
                <el-input v-model.number="returnBookInfo.cardid" style="width: 12.5vw;" clearable />
            </div>
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                还书时间
                <el-input v-model="formattedTimestamp" style="width: 12.5vw;" disabled />
            </div>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="returnBookVisible = false">取消</el-button>
                    <el-button type="primary" @click="returnBook">确定</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 删除图书对话框 -->
        <el-dialog v-model="removeBookVisible" title="删除图书" width="30%">
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                确认删除编号为 {{ this.toRemove }} 的图书吗？
            </div>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="this.removeBookVisible = false">取消</el-button>
                    <el-button type="primary" @click="this.removeBook">确定</el-button>
                </span>
            </template>
        </el-dialog>


        <!-- 增加库存对话框 -->
        <el-dialog v-model="this.incstockVisible" title="增加库存" width="30%">
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                编号
                <el-input v-model="this.selectedBook.id" style="width: 12.5vw;" disabled />
            </div>
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                增加库存
                <el-input v-model.number="this.selectedBook.stock" style="width: 12.5vw;" clearable />
            </div>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="this.incstockVisible = false">取消</el-button>
                    <el-button type="primary" @click="incstock">确定</el-button>
                </span>
            </template>

        </el-dialog>

        <!-- 借书对话框 -->
        <el-dialog v-model="this.borrowVisible" title="借书" width="30%">
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                编号
                <el-input v-model="this.borrow.id" style="width: 12.5vw;" disabled />
            </div>
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
            时间
            <el-input v-model="formattedTimestamp" style="width: 12.5vw;" disabled />
        </div>
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                借书证编号
                <el-input v-model.number="this.borrow.cardid" style="width: 12.5vw;" clearable />
            </div>


            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="this.borrowVisible = false">取消</el-button>
                    <el-button type="primary" @click="borrowbook">确定</el-button>
                </span>
            </template>

        </el-dialog>

        <!-- 修改图书对话框 -->  
        <el-dialog v-model="modifyBookVisible" title="修改信息" width="30%">
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                编号
                <el-input v-model="modifyBookInfo.id" style="width: 12.5vw;" disabled />
            </div>
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                类型
                <el-input v-model="modifyBookInfo.Category" style="width: 12.5vw;" clearable />
            </div>
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                书名
                <el-input v-model="modifyBookInfo.Title" style="width: 12.5vw;" clearable />
            </div>
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                出版社
                <el-input v-model="modifyBookInfo.Press" style="width: 12.5vw;" clearable />
            </div>
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                出版年份
                <el-input v-model.number="modifyBookInfo.Publishyear" style="width: 12.5vw;" clearable />
            </div>
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                作者
                <el-input v-model="modifyBookInfo.Author" style="width: 12.5vw;" clearable />
            </div>
            <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                价格
                <el-input v-model.number="modifyBookInfo.Price" style="width: 12.5vw;" clearable />
            </div>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="modifyBookVisible = false">取消</el-button>
                    <el-button type="primary" @click="modifyBook">确定</el-button>
                </span>
            </template>
        </el-dialog>

        
        
  
    </el-scrollbar>




</template>



<script>
import { Delete, Edit, Search, Check,Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'


export default {
    data() {
        return {
            Delete,
            Edit,
            Search,
            Check,
            Star,
            toSearch: '', // 搜索内容
            books: [{
                id : 1,
                category: '计算机',
                title: '计算机网络',
                press: '清华大学出版社',
                publishyear: '2021',
                author: '谢希仁',
                price: '50',
                stock: '100'
            }], // 图书列表
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
            modifyBookInfo: { // 待新建图书信息
                id : '',
                Category: '',
                Title: '',
                Press: '',
                Publishyear: '',
                Author: '',
                Price: '',
            },
            incstockVisible: false, // 增加库存对话框可见性
            selectedBook: {
                id: '',
                deltastock: 0
            },// 选中的图书
            borrow :{
                id: '',
                time: Date.now(),
                cardid:0
            },
            borrowVisible: false, // 借书对话框可见性
            currentTimestamp: Date.now(),
            removeBookVisible: false ,// 删除图书对话框可见性
            toRemove: 0, // 待删除图书号
            returnBookVisible: false, // 还书对话框可见性
            returnBookInfo: { // 待还书信息
                id: '',
                cardid: ''
            },
            uploadVisible: false,
            fileList: []        
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
                    this.Refresh()
                    this.newBookVisible = false // 将对话框设置为不可见
                })
                .catch(error => {
                    ElMessage.error(error.response.data) // 显示消息提醒
                })
        },
        QueryBook() {
            // 发出POST请求
            this.books = []
            axios.post("/book/",
                { // 请求体
                    action: "querybook",
                    Category: this.queryBookInfo.Category,
                    Title: this.queryBookInfo.Title,
                    Press: this.queryBookInfo.Press,
                    MinPublishyear: this.queryBookInfo.MinPublishyear,
                    MaxPublishyear: this.queryBookInfo.MaxPublishyear,
                    Author: this.queryBookInfo.Author,
                    MinPrice: this.queryBookInfo.MinPrice,
                    MaxPrice: this.queryBookInfo.MaxPrice
                })
                .then(response => {
                    let books = response.data
                    books.forEach(book => {
                        this.books.push(book)       
                    })
                    ElMessage.success("查询成功")
                    this.queryBookVisible = false // 将对话框设置为不可见
                })
                .catch(error => {
                    ElMessage.error("查询失败") // 显示消息提醒
                })
        },
        Refresh(){
            this.books = []
            axios.get("/book/")
                .then(response => {
                    let books = response.data
                    books.forEach(book => {
                        this.books.push(book)       
                    })
                })
                .catch(error => {
                    ElMessage.error("查询失败") // 显示消息提醒
                })
        },
        modifyBook(){
            axios.post("/book/",
            { // 请求体
                action: "modifybook",
                id: this.modifyBookInfo.id,
                Category: this.modifyBookInfo.Category,
                Title: this.modifyBookInfo.Title,
                Press: this.modifyBookInfo.Press,
                Publishyear: this.modifyBookInfo.Publishyear,
                Author: this.modifyBookInfo.Author,
                Price: this.modifyBookInfo.Price
            })
            .then(response => {
                ElMessage.success("修改成功") // 显示消息提醒
                this.Refresh()
                this.modifyBookVisible = false // 将对话框设置为不可见
            })
            .catch(error => {
                ElMessage.error(error.response.data) // 显示消息提醒
            })
        },
        incstock(){
            axios.post("/book/",
            { // 请求体
                action: "incstock",
                id: this.selectedBook.id,
                deltastock: this.selectedBook.stock
            })
            .then(response => {
                ElMessage.success("增加库存成功") // 显示消息提醒
                this.Refresh()
                this.incstockVisible = false // 将对话框设置为不可见
            })
            .catch(error =>{
                ElMessage.error(error.response.data)
            })
        },
        borrowbook(){
            axios.post("/book/",
            {
                action: "borrowbook",
                id: this.borrow.id,
                cardid: this.borrow.cardid,
                time: this.currentTimestamp
            })
            .then(response => {
                ElMessage.success("借书成功！")
                this.Refresh()
                this.borrowVisible = false
            })
            .catch(error =>{
                ElMessage.error(error.response.data)
             })
        },
        removeBook(){
            axios.post("/book/",
            {
                action: "removebook",
                id: this.toRemove
            })
            .then(response => {
                ElMessage.success("删除成功！")
                this.Refresh()
                this.removeBookVisible = false
            })
            .catch(error =>{
                ElMessage.error(error.response.data)
            })
        },
        returnBook(){
            axios.post("/book/",
            {
                action: "returnbook",
                id: this.returnBookInfo.id,
                cardid: this.returnBookInfo.cardid,
                time: this.currentTimestamp
            })
            .then(response => {
                ElMessage.success("还书成功！")
                this.Refresh()
                this.returnBookVisible = false
            })
            .catch(error =>{
                ElMessage.error(error.response.data)
            })
        },

    },
    mounted() {
        this.Refresh()
            setInterval(() => {
        this.currentTimestamp = Date.now();
        }, 1000);
    },
    computed: {
    formattedTimestamp() {
      const date = new Date(this.currentTimestamp);
      const year = date.getFullYear();
      const month = date.getMonth() + 1;
      const day = date.getDate();
      const hours = date.getHours();
      const minutes = date.getMinutes();
      const seconds = date.getSeconds();
      return `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')} ${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
       }
    }

    // mounted() {
    //     this.QueryBook() // 查询借书证以刷新页面
    // }
}   

</script>


<style scoped>
.bookBox {
    height: 450px;
    width: 250px;
    box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
    text-align: center;
    margin-top: 20px;
    margin-left: 27.5px;
    margin-right: 10px;
    padding: 7.5px;
    padding-right: 10px;
    padding-top: 10px;
    size: 10px;
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
