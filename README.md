# 简介
这是一款使用 Compose 实现的快速为照片添加时间水印的桌面端应用。

使用 Compose-multiplatform （原 Compose-jb）实现，所以支持所有桌面端（Windows、macOS、Linux）。

适合用于为制作延时视频而拍摄的照片添加时间信息。

通过读取照片 EXif 中的拍摄时间信息来为您的照片添加时间水印。

支持自定义水印文字格式、自定义水印位置、自定义文字大小、自定义文字颜色等。

支持一键拖拽文件夹或文件批量添加。

# 截图
![1](./docs/1.png)

![2](./docs/2.png)

![3](./docs/3.png)


# 实现思路和技术细节
1. [Compose For Desktop 实践：使用 Compose-jb 做一个时间水印助手](https://juejin.cn/post/7165387863129849864)
2. [为 Compose 的 TextField 添加类似 EditText inputType 的输入过滤](https://juejin.cn/post/7214314627521282085)

# 参考资料
1. [使用ComposeDesktop开发一款桌面端多功能APK工具](https://juejin.cn/post/7122645579439538183)
2. [From Swing to Jetpack Compose Desktop #2](https://dev.to/tkuenneth/from-swing-to-jetpack-compose-desktop-2-4a4h)
3. [Java中图片添加水印（文字+图片水印）](https://juejin.cn/post/6982192057209077791)
4. [metadata-extractor](https://github.com/drewnoakes/metadata-extractor)
5. [Image and in-app icons manipulations](https://github.com/JetBrains/compose-jb/tree/master/tutorials/Image_And_Icons_Manipulations)