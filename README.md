# CompileAnnotation
编译时注解框架demo，实现了视图绑定，点击事件绑定。可以用在activity,fragment和adapter等各种场景
实现activity和fragment之间的快速传值

![IMAGE](https://github.com/GodisGod/CompileAnnotation/blob/master/test.jpg)

使用方法：

视图绑定和点击事件绑定功能

    @BindView(R.id.tv_test)
    TextView textView;

    @BindView(R.id.btn_test)
    Button btnTest;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        //视图绑定和点击事件绑定功能需要
        DInject.inject(this);
        QSecondActivity.inject(this);
   }
    
    //绑定多个点击事件
    @ClickEvents({R.id.btn_test, R.id.tv_test})
    public void allClickTest(View v) {
    }
    //绑定单个点击事件
    @ClickEvent(R.id.btn_test)
    public void onClickTest() {}
    @LongClickEvent(R.id.tv_test)
    public void onLongClickTest()
    
快速传值功能：
public class SecondActivity extends AppCompatActivity {
    //在需要传递的参数上使用注解
    @QJump
    String name;
    @QJump
    int value;
    @QJump
    TestBean testBean;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        //构建代码以后会生成“QXXXX”类，然后调用此类的inject方法即可完成注册
        QSecondActivity.inject(this);
        //然后就可以直接使用参数啦
        Log.i("test","name = "+name+" value = "+value);
   }
   
}
    


