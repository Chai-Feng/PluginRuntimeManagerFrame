package com.manager.demo.util;

/**
 * @Author: Alex
 * @Email: chai5885@gmail.com
 * @Description:
 * @Date: 2023/4/22 14:11
 */
public class TestFather {
    static String st = "静态变量测试";
    String sv = "普通变量";

    static {
        System.out.println("我是静态代码块");
    }

    public static void main(String[] args) {
        new Son().printDetil();
        System.out.println();
//        Father f=new Son();
//        f.printDetil();
//        System.out.println();
//        new Father().printDetil();
    }

    public void sayHello() {
        System.out.println("hello!!~~~~");
    }

}


class Father extends GranPa {

    public void printDetil() {
        System.out.println("123456");
    }
}

class Son extends Father {


    public void printDetil() {
        super.printDetil();
        super.yeye();
        System.out.println("this --- " + this.getClass().getName());
        //Class<? extends Son> aClass = this.getClass();
        System.out.println("super --- " + this.getClass().getSuperclass());
    }
}

class GranPa {

    public void yeye() {
        System.out.println("我是 嫩爷爷");
    }

    public void printDetil() {
        System.out.println("爷爷的123456");
    }
}