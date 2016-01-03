# PicTalk

### 仕様

PicTalkは、画像と音声を組み合わせたものをやりとりするクローズドなチャットサービスです。
現在はMVPである画像と音声をポストし、表示・再生する機能を実装しています。
今後はクローズドなチャットを実装する予定です。


### 依存関係

```groovy
dependencies {
  compile fileTree(dir: 'libs', include: ['*.jar'])
  testCompile 'junit:junit:4.12'
  compile 'com.android.support:appcompat-v7:23.1.1'
  compile 'com.jakewharton:butterknife:7.0.1'
  compile 'com.squareup.retrofit:retrofit:2.0.0-beta2'
  compile 'com.squareup.retrofit:converter-gson:2.0.0-beta2'
}
```


### 工夫した点
今後スタンダードになると思われる以下の新しい技術を盛り込みました。
- Lollipopで導入されたCamera2 API
- Marshmallowで導入されたPermission API
- ButterKnife 7
- Retrofit 2
