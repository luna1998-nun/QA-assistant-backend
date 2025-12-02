import os
import sys
import shutil
import site
import subprocess
import nltk
from pathlib import Path

def install_package(package):
    print(f"正在安装 {package}...")
    try:
        subprocess.check_call([sys.executable, "-m", "pip", "install", package, "-i", "https://pypi.tuna.tsinghua.edu.cn/simple"])
        print(f"✅ {package} 安装成功")
    except subprocess.CalledProcessError:
        print(f"❌ {package} 安装失败，请手动运行: pip install {package}")

def fix_unidic():
    print("\n=== 1. 修复 Unidic 字典 (使用 unidic-lite 替换方案) ===")
    
    # 1. 确保安装了 unidic 和 unidic-lite
    try:
        import unidic
    except ImportError:
        install_package("unidic")
        import unidic

    try:
        import unidic_lite
    except ImportError:
        install_package("unidic-lite")
        import unidic_lite

    # 2. 获取路径
    unidic_path = Path(unidic.__path__[0])
    lite_path = Path(unidic_lite.__path__[0])
    
    print(f"Unidic 路径: {unidic_path}")
    print(f"Unidic-lite 路径: {lite_path}")

    # 3. 复制字典文件 (参考文章中的简单处理方案)
    # 这里的逻辑是：MeloTTS 依赖 unidic，但 unidic 默认是空的需要下载。
    # unidic-lite 自带字典，我们把它拷过去骗过检查。
    
    target_dicdir = unidic_path / "dicdir"
    source_dicdir = lite_path / "dicdir"

    if not source_dicdir.exists():
        print("❌ 错误: 找不到 unidic-lite 的字典目录，请确保 pip install unidic-lite 成功")
        return

    if target_dicdir.exists():
        print(f"⚠️ 目标目录已存在: {target_dicdir}")
        # 检查是否为空
        if any(target_dicdir.iterdir()):
            print("   目录不为空，假设已修复。如仍有问题，请手动删除该目录后重试。")
        else:
            print("   目录为空，正在复制...")
            shutil.rmtree(target_dicdir)
            shutil.copytree(source_dicdir, target_dicdir)
            print("✅ 字典复制完成")
    else:
        print("正在从 unidic-lite 复制字典数据到 unidic...")
        shutil.copytree(source_dicdir, target_dicdir)
        print("✅ 字典复制完成")

    # 4. 创建必要的 version 文件防止报错
    version_file = unidic_path / "version"
    if not version_file.exists():
        with open(version_file, "w") as f:
            f.write("3.1.0")
        print("✅ 已创建 version 文件")

def fix_nltk():
    print("\n=== 2. 下载 NLTK 数据 ===")
    # 强制设置 NLTK 下载目录到用户目录，避免权限问题
    nltk_data_dir = os.path.expanduser("~/nltk_data")
    if not os.path.exists(nltk_data_dir):
        os.makedirs(nltk_data_dir)
    
    nltk.data.path.append(nltk_data_dir)
    
    packages = ['averaged_perceptron_tagger', 'cmudict']
    
    for pkg in packages:
        print(f"正在检查/下载 {pkg}...")
        try:
            # 尝试正常下载
            nltk.download(pkg, download_dir=nltk_data_dir, quiet=True)
            print(f"✅ {pkg} 下载成功")
        except Exception as e:
            print(f"⚠️ {pkg} 自动下载失败: {e}")
            print("尝试使用备用镜像源或手动下载...")
            # 这里如果不成功，建议用户手动下载，避免脚本卡死
            print(f"   [手动下载指引] 请访问 https://github.com/nltk/nltk_data/tree/gh-pages/packages")
            print(f"   下载 {pkg}.zip 并解压到 {nltk_data_dir} 目录下")

def main():
    print("开始环境修复流程...")
    
    # 1. 设置 Hugging Face 镜像
    os.environ["HF_ENDPOINT"] = "https://hf-mirror.com"
    print(f"\n=== 0. 设置环境变量 ===\nHF_ENDPOINT = {os.environ['HF_ENDPOINT']}")
    
    fix_unidic()
    fix_nltk()
    
    print("\n✅ 修复流程结束。请尝试运行 main.py")

if __name__ == "__main__":
    main()