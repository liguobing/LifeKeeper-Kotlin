package com.lixyz.lifekeeperforkotlin.sql

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory

import android.database.sqlite.SQLiteOpenHelper


class SQLiteHelper(
    context: Context?,
    name: String?,
    factory: CursorFactory?,
    version: Int
) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        //消费账单
        db.execSQL(CREATE_BILL)
        //消费分类
        db.execSQL(CREATE_BILL_CATEGORY)
        //账户
        db.execSQL(CREATE_BILL_ACCOUNT)
        //商家
        db.execSQL(CREATE_BILL_SHOP)
        //计划
        db.execSQL(CREATE_PLAN_TABLE)

        db.execSQL(CREATE_RECORD)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
    }

    companion object {
        private const val CREATE_RECORD = "create table if not exists PhoneRecord(FileName text,ContactName text)"

        private const val CREATE_BILL = "create table if not exists Bill(" +
                "ObjectId text primary key," +  //唯一标识
                "BillId text," +  //账单ID
                "BillMoney real," +  //账单金额
                "BillProperty integer," +  //账单属性  1：收入  -1：支出"
                "BillCategory text," +  //账单种类
                "BillAccount text," +  //账单账户
                "BillRemark text," +  //账单备注
                "BillUser text," +  //账单用户
                "BillShop text," +  //账单位置
                "BillStatus integer," +  //账单状态  1:正常账单   -1：非正常账单
                "BillType integer," +  //账单类型  0：正常  1：已删除  2：已修改"
                "BillDate real," +  //消费时间
                "CreateTime real," +  //创建时间
                "UpdateTime real," +  //更新时间
                "BillImage text)" //账单图片
        private const val CREATE_BILL_CATEGORY =
            "create table if not exists BillCategory(" +
                    "ObjectId text primary key," +  //ObjectId 唯一标识
                    "CategoryId text," +  //种类ID
                    "CategoryUser text," +  //创建种类的用户ID
                    "CategoryName text," +  //种类名称
                    "IsIncome integer," +  //是收入吗？  1：收入   -1：支出
                    "CategoryStatus integer," +  //种类状态  1：正常   -1：非正常
                    "CategoryType integer," +  //种类类型 0：正常    1：已删除   2：已修改
                    "CreateTime real," +  //创建时间
                    "UpdateTime real," +  //更新时间
                    "OrderIndex integer)" //排序下标
        private const val CREATE_BILL_ACCOUNT =
            "create table if not exists BillAccount(" +
                    "ObjectId text primary key," +  //ObjectId 唯一标识
                    "AccountId text," +  //账户ID
                    "AccountUser text," +  //创建账户的用户ID
                    "AccountName text," +  //账户名
                    "AccountStatus integer," +  //账户状态	1：正常  -1：非正常
                    "AccountType integer," +  //账户类型：	0：正常	1：已删除	2：已修改
                    "CreateTime real," +  //创建时间
                    "UpdateTime real," +  //更新时间
                    "OrderIndex integer)" //排序下标
        private const val CREATE_BILL_SHOP = "create table if not exists BillShop(" +
                "ObjectId text primary key," +  //ObjectId 唯一标识
                "ShopId text," +  //商家ID
                "ShopName text," +  //商家名称
                "ShopIcon text," +  //商家图标
                "ShopUser text," +  //创建商家的用户
                "ShopStatus integer," +  //商家状态	1：正常  -1：非正常
                "ShopType integer," +  //商家类型：	0：正常	1：已删除	2：已修改
                "CreateTime real," +  //创建时间
                "UpdateTime real," +//更新时间
                "OrderIndex integer)" //排序下标
        private const val CREATE_PLAN_TABLE = "create table if not exists PlanTable(" +
                "ObjectId text primary key," +
                "PlanId text," +
                "GroupId text," +
                "IsAllDay integer," +
                "PlanName text," +
                "PlanDescription text," +
                "PlanLocation text," +
                "PlanUser text," +
                "StartTime real," +
                "RepeatType integer," +
                "EndRepeatType integer," +
                "EndRepeatValue real," +
                "AlarmTime integer," +
                "IsFinished integer," +
                "PlanStatus integer," +
                "PlanType integer," +
                "CreateTime real," +
                "UpdateTime real," +
                "FinishTime real)"
    }
}