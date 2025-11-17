<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
        <div class="shape shape-5"></div>
      </div>
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo-container">
            <div class="logo-wrapper">
              <img src="@/assets/images/logo.png" alt="长庆油田" class="brand-logo" />
              <div class="logo-ring"></div>
            </div>
          </div>
          
          <div class="brand-text">
            <h1 class="brand-title">长庆油田智能生产运营平台</h1>
            <p class="brand-subtitle">Intelligent Production Operation Platform</p>
            <div class="brand-description">
              <p>基于人工智能技术的现代化油田生产管理系统</p>
              <p>提升生产效率，保障安全生产，实现智能化运营</p>
            </div>
          </div>

          <!-- 功能特色 -->
          <div class="features-grid">
            <div class="feature-card">
              <div class="feature-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M9 12l2 2 4-4"/>
                  <circle cx="12" cy="12" r="10"/>
                </svg>
              </div>
              <h3>智能监控</h3>
              <p>实时监控生产数据</p>
            </div>
            
            <div class="feature-card">
              <div class="feature-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
                </svg>
              </div>
              <h3>数据分析</h3>
              <p>深度挖掘生产数据</p>
            </div>
            
            <div class="feature-card">
              <div class="feature-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                </svg>
              </div>
              <h3>优化运营</h3>
              <p>智能优化生产流程</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录区域 -->
      <div class="login-section">
        <div class="login-container">
          <div class="login-card">
            <div class="card-header">
              <h2 class="login-title">系统登录</h2>
              <p class="login-subtitle">欢迎使用智能生产运营平台</p>
            </div>

        <n-form
          ref="formRef"
          label-placement="left"
          size="large"
          :model="formInline"
          :rules="rules"
          class="login-form"
        >
              <n-form-item path="username" class="form-item">
            <n-input 
              v-model:value="formInline.username" 
              placeholder="请输入用户名"
                  class="modern-input"
            >
              <template #prefix>
                    <n-icon size="20" color="#6366f1">
                  <PersonOutline />
                </n-icon>
              </template>
            </n-input>
          </n-form-item>

              <n-form-item path="password" class="form-item">
            <n-input
              v-model:value="formInline.password"
              type="password"
              showPasswordOn="click"
              placeholder="请输入密码"
                  class="modern-input"
            >
              <template #prefix>
                    <n-icon size="20" color="#6366f1">
                  <LockClosedOutline />
                </n-icon>
              </template>
            </n-input>
          </n-form-item>

              <n-form-item class="form-options">
                <div class="options-wrapper">
                  <div class="remember-me">
                    <n-checkbox v-model:checked="autoLogin" class="custom-checkbox">
                      记住登录状态
                    </n-checkbox>
              </div>
                  <div class="forgot-password">
                    <a href="javascript:" class="forgot-link">忘记密码？</a>
              </div>
            </div>
          </n-form-item>

              <n-form-item class="login-button-item">
            <n-button 
              type="primary" 
              @click="handleSubmit" 
              size="large" 
              :loading="loading" 
              block
                  class="modern-button"
            >
                  <span class="button-text">登录系统</span>
                  <div class="button-shine"></div>
            </n-button>
          </n-form-item>
        </n-form>

            <div class="card-footer">
              <p class="footer-text">© 2024 长庆油田智能生产运营平台 v3.0.0</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { reactive, ref, onMounted } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useUserStore } from '@/store/modules/user';
  import { useMessage } from 'naive-ui';
  import { ResultEnum } from '@/enums/httpEnum';
  import { PersonOutline, LockClosedOutline } from '@vicons/ionicons5';
  import { PageEnum } from '@/enums/pageEnum';
  
  // 添加页面加载动画效果
  onMounted(() => {
    // 聚焦用户名输入框
    setTimeout(() => {
      const usernameInput = document.querySelector('input[placeholder="请输入用户名"]');
      if (usernameInput) {
        (usernameInput as HTMLElement).focus();
      }
    }, 500);
  });
  interface FormState {
    username: string;
    password: string;
  }

  const formRef = ref();
  const message = useMessage();
  const loading = ref(false);
  const autoLogin = ref(true);
  const LOGIN_NAME = PageEnum.BASE_LOGIN_NAME;

  const formInline = reactive({
    username: 'admin',
    password: '%￥%#￥……&FDSFSDGFSDFDSFSDFDSFD',
    isCaptcha: true,
  });

  const rules = {
    username: { required: true, message: '请输入用户名', trigger: 'blur' },
    password: { required: true, message: '请输入密码', trigger: 'blur' },
  };

  const userStore = useUserStore();

  const router = useRouter();
  const route = useRoute();

  const handleSubmit = (e) => {
    e.preventDefault();
    formRef.value.validate(async (errors) => {
      if (!errors) {
        const { username, password } = formInline;
        message.loading('登录中...');
        loading.value = true;

        const params: FormState = {
          username,
          password,
        };

        try {
          const { code, message: msg } = await userStore.login(params);
          message.destroyAll();
          if (code == ResultEnum.SUCCESS) {
            const toPath = decodeURIComponent((route.query?.redirect || '/') as string);
            message.success('登录成功，即将进入系统');
            if (route.name === LOGIN_NAME) {
              router.replace('/');
            } else router.replace(toPath);
          } else {
            message.info(msg || '登录失败');
          }
        } finally {
          loading.value = false;
        }
      } else {
        message.error('请填写完整信息，并且进行验证码校验');
      }
    });
  };
</script>

<style lang="less" scoped>
  .login-page {
    min-height: 100vh;
    background: linear-gradient(135deg, #0f0f23 0%, #1a1a2e 25%, #16213e 50%, #1a1a2e 75%, #0f0f23 100%);
    font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
    position: relative;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
  }
    
  // 背景装饰
  .background-decoration {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
    z-index: 1;
    overflow: hidden;
    
    .floating-shapes {
      position: absolute;
      width: 100%;
      height: 100%;
      
      .shape {
        position: absolute;
        border-radius: 50%;
        background: linear-gradient(45deg, rgba(99, 102, 241, 0.1), rgba(139, 92, 246, 0.1));
        animation: float 8s ease-in-out infinite;
        
        &.shape-1 {
          width: 80px;
          height: 80px;
          top: 20%;
          left: 10%;
          animation-delay: 0s;
        }
        
        &.shape-2 {
          width: 120px;
          height: 120px;
          top: 60%;
          right: 15%;
          animation-delay: 2s;
        }
        
        &.shape-3 {
          width: 60px;
          height: 60px;
          bottom: 30%;
          left: 20%;
          animation-delay: 4s;
        }
        
        &.shape-4 {
          width: 100px;
          height: 100px;
          top: 40%;
          right: 30%;
          animation-delay: 1s;
        }
        
        &.shape-5 {
          width: 140px;
          height: 140px;
          bottom: 10%;
          right: 10%;
          animation-delay: 3s;
        }
      }
    }
    
    .gradient-orb {
      position: absolute;
      border-radius: 50%;
      filter: blur(40px);
      animation: pulse 6s ease-in-out infinite;
      
      &.orb-1 {
        width: 300px;
        height: 300px;
        background: radial-gradient(circle, rgba(99, 102, 241, 0.3) 0%, transparent 70%);
        top: -150px;
        left: -150px;
        animation-delay: 0s;
      }
      
      &.orb-2 {
        width: 200px;
        height: 200px;
        background: radial-gradient(circle, rgba(139, 92, 246, 0.3) 0%, transparent 70%);
        bottom: -100px;
        right: -100px;
        animation-delay: 2s;
      }
      
      &.orb-3 {
        width: 250px;
        height: 250px;
        background: radial-gradient(circle, rgba(59, 130, 246, 0.2) 0%, transparent 70%);
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        animation-delay: 4s;
      }
    }
  }

  // 主要内容区域
  .main-content {
    display: flex;
    width: 100%;
    max-width: 1400px;
    min-height: 100vh;
    position: relative;
    z-index: 2;
  }

  // 品牌区域
  .brand-section {
    flex: 0 0 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 60px 40px;
    position: relative;
    
    .brand-content {
      max-width: 500px;
      width: 100%;
    }
    
    .logo-container {
      text-align: center;
      margin-bottom: 40px;
      
      .logo-wrapper {
        position: relative;
        display: inline-block;
        
        .brand-logo {
          width: 100px;
          height: 100px;
          border-radius: 50%;
          box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
          transition: all 0.3s ease;
          position: relative;
          z-index: 2;
          
          &:hover {
            transform: scale(1.05);
            box-shadow: 0 25px 50px rgba(0, 0, 0, 0.4);
          }
        }
        
        .logo-ring {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          width: 120px;
          height: 120px;
          border: 2px solid rgba(99, 102, 241, 0.3);
          border-radius: 50%;
          animation: rotate 10s linear infinite;
          
          &::before {
        content: '';
        position: absolute;
            top: -2px;
            left: -2px;
            right: -2px;
            bottom: -2px;
            border: 2px solid rgba(139, 92, 246, 0.2);
            border-radius: 50%;
            animation: rotate 15s linear infinite reverse;
          }
        }
      }
    }
    
    .brand-text {
      text-align: center;
      margin-bottom: 50px;
      
      .brand-title {
        font-size: 36px;
        font-weight: 700;
        color: #ffffff;
        margin: 0 0 15px 0;
        text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
        line-height: 1.2;
        background: linear-gradient(135deg, #ffffff 0%, #e0e7ff 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
      
      .brand-subtitle {
        font-size: 16px;
        color: rgba(255, 255, 255, 0.7);
        margin: 0 0 25px 0;
        font-weight: 300;
        letter-spacing: 1px;
      }
      
      .brand-description {
        p {
          font-size: 16px;
          color: rgba(255, 255, 255, 0.6);
          margin: 0 0 10px 0;
          line-height: 1.6;
          
          &:last-child {
            margin-bottom: 0;
          }
        }
      }
    }
    
    .features-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 20px;
      
      .feature-card {
        background: rgba(255, 255, 255, 0.05);
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 16px;
        padding: 20px;
      text-align: center;
        transition: all 0.3s ease;
        backdrop-filter: blur(10px);
        
        &:hover {
          transform: translateY(-5px);
          background: rgba(255, 255, 255, 0.1);
          border-color: rgba(99, 102, 241, 0.3);
          box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
        }
        
        .feature-icon {
          width: 40px;
          height: 40px;
          background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin: 0 auto 15px;
          box-shadow: 0 4px 15px rgba(99, 102, 241, 0.3);
          
          svg {
            width: 20px;
            height: 20px;
            color: #ffffff;
          }
        }
        
        h3 {
        font-size: 14px;
          font-weight: 600;
          color: #ffffff;
          margin: 0 0 8px 0;
        }
        
        p {
          font-size: 12px;
          color: rgba(255, 255, 255, 0.6);
          margin: 0;
          line-height: 1.4;
        }
      }
    }
  }

  // 登录区域
  .login-section {
    flex: 0 0 50%;
      display: flex;
      align-items: center;
    justify-content: center;
    padding: 40px;
    position: relative;
  }

  .login-container {
    width: 100%;
    max-width: 400px;
    z-index: 2;
    position: relative;
  }

  .login-card {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 24px;
    padding: 40px;
    box-shadow: 
      0 25px 80px rgba(0, 0, 0, 0.3),
      0 10px 30px rgba(0, 0, 0, 0.2),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(20px);
    width: 100%;
    position: relative;
  }

  .card-header {
    text-align: center;
    margin-bottom: 40px;
    
    .login-title {
      font-size: 28px;
      font-weight: 700;
      color: #ffffff;
      margin: 0 0 10px 0;
      position: relative;
      text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
      
      &::after {
        content: '';
        position: absolute;
        bottom: -8px;
        left: 50%;
        transform: translateX(-50%);
        width: 50px;
        height: 2px;
        background: linear-gradient(90deg, #6366f1 0%, #8b5cf6 100%);
        border-radius: 2px;
      }
    }
    
    .login-subtitle {
      font-size: 16px;
      color: rgba(255, 255, 255, 0.7);
      margin: 0;
      font-weight: 400;
    }
  }

  // 表单样式
  .login-form {
    .form-item {
      margin-bottom: 24px;
      
      :deep(.n-form-item-feedback-wrapper) {
        min-height: 18px;
      }
    }
    
    .custom-input {
      :deep(.n-input-wrapper) {
        background: rgba(255, 255, 255, 0.2);
        border: 2px solid rgba(255, 255, 255, 0.3);
        border-radius: 16px;
        transition: all 0.3s ease;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        height: 56px;
        backdrop-filter: blur(10px);
        
        &:hover {
          border-color: rgba(255, 255, 255, 0.5);
          box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
          transform: translateY(-1px);
          background: rgba(255, 255, 255, 0.25);
        }
        
        &.n-input--focus {
          border-color: #ffffff;
          box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.2), 0 6px 20px rgba(0, 0, 0, 0.15);
          transform: translateY(-2px);
          background: rgba(255, 255, 255, 0.3);
        }
      }
      
      :deep(.n-input__input-el) {
        color: #ffffff;
        font-size: 16px;
        font-weight: 500;
        padding: 0 20px;
        
        &::placeholder {
          color: rgba(255, 255, 255, 0.6);
          font-weight: 400;
        }
      }
      
      :deep(.n-input__prefix) {
        padding-left: 20px;
      }
    }
    
    .form-options {
      margin-bottom: 30px;
      
      .options-wrapper {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;
      }
      
      .custom-checkbox {
        :deep(.n-checkbox__label) {
          color: #6b7280;
          font-size: 14px;
        }
      }
      
      .forgot-link {
        color: #6366f1;
        text-decoration: none;
        font-size: 14px;
        font-weight: 500;
        transition: all 0.3s ease;
      
      &:hover {
          color: #4f46e5;
          text-decoration: underline;
        }
      }
    }
    
    .login-button-item {
      margin-bottom: 0;
    }
  }

  // 登录按钮
  .login-button {
    height: 56px;
    border-radius: 16px;
    background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
    border: none;
    font-size: 18px;
    font-weight: 600;
    color: #ffffff;
    transition: all 0.3s ease;
    box-shadow: 0 6px 20px rgba(99, 102, 241, 0.3);
      position: relative;
    overflow: hidden;
      
      &::before {
        content: '';
        position: absolute;
        top: 0;
      left: -100%;
        width: 100%;
        height: 100%;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
      transition: left 0.5s ease;
    }
    
    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 12px 30px rgba(99, 102, 241, 0.4);
      background: linear-gradient(135deg, #5b21b6 0%, #7c3aed 100%);
      
      &::before {
        left: 100%;
      }
    }
    
    &:active {
      transform: translateY(-1px);
    }
  }

  // 底部信息
  .card-footer {
    margin-top: 30px;
    text-align: center;
    
    .footer-text {
      font-size: 12px;
      color: rgba(255, 255, 255, 0.5);
      margin: 0;
    }
  }

  // 动画定义
  @keyframes float {
    0%, 100% {
      transform: translateY(0px);
    }
    50% {
      transform: translateY(-20px);
    }
  }

  @keyframes pulse {
    0%, 100% {
      transform: scale(1);
      opacity: 0.3;
    }
    50% {
      transform: scale(1.1);
      opacity: 0.5;
    }
  }

  @keyframes rotate {
    0% {
      transform: translate(-50%, -50%) rotate(0deg);
    }
    100% {
      transform: translate(-50%, -50%) rotate(360deg);
    }
  }

  // 响应式设计
  @media (max-width: 1200px) {
    .main-content {
      flex-direction: column;
    }
    
    .brand-section {
      flex: none;
      padding: 40px 30px;
    }
    
    .login-section {
      flex: none;
      padding: 30px;
    }
  }

  @media (max-width: 768px) {
    .brand-section {
      padding: 30px 20px;
      
      .brand-content {
        max-width: 100%;
      }
      
      .brand-title {
        font-size: 28px;
      }
      
      .features-grid {
        grid-template-columns: 1fr;
        gap: 15px;
      }
    }
    
    .login-section {
      padding: 20px;
    }
    
    .login-card {
      padding: 30px 25px;
    }
  }

  @media (max-width: 480px) {
    .brand-section {
      padding: 20px 15px;
      
      .logo-container .brand-logo {
        width: 80px;
        height: 80px;
      }
      
      .brand-title {
        font-size: 24px;
      }
      
      .brand-subtitle {
        font-size: 14px;
      }
      
      .brand-description p {
        font-size: 14px;
      }
    }
    
    .login-card {
      padding: 25px 20px;
    }
    
    .card-header .login-title {
      font-size: 24px;
    }
  }

  // 背景装饰
  .background-decoration {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
    z-index: 1;
    
    .decoration-circle {
      position: absolute;
      border-radius: 50%;
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
      animation: float 6s ease-in-out infinite;
      
      &.circle-1 {
        width: 200px;
        height: 200px;
        top: 10%;
        left: 10%;
        animation-delay: 0s;
      }
      
      &.circle-2 {
        width: 150px;
        height: 150px;
        top: 60%;
        right: 15%;
        animation-delay: 2s;
      }
      
      &.circle-3 {
        width: 100px;
        height: 100px;
        bottom: 20%;
        left: 20%;
        animation-delay: 4s;
      }
    }
    
    .decoration-line {
      position: absolute;
      background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.2) 50%, transparent 100%);
      height: 1px;
      animation: lineMove 8s ease-in-out infinite;
      
      &.line-1 {
        top: 30%;
        left: 0;
        right: 0;
        animation-delay: 0s;
      }
      
      &.line-2 {
        top: 70%;
        left: 0;
        right: 0;
        animation-delay: 4s;
      }
    }
  }

  // 动画定义
  @keyframes pulse {
    0%, 100% {
      transform: translate(-50%, -50%) scale(1);
      opacity: 0.3;
    }
    50% {
      transform: translate(-50%, -50%) scale(1.1);
      opacity: 0.5;
    }
  }

  @keyframes float {
    0%, 100% {
      transform: translateY(0px);
    }
    50% {
      transform: translateY(-20px);
    }
  }

  @keyframes lineMove {
    0%, 100% {
      opacity: 0;
      transform: translateX(-100%);
    }
    50% {
      opacity: 1;
      transform: translateX(0);
    }
  }

  // 响应式设计
  @media (max-width: 1200px) {
    .login-left {
      flex: 0 0 55%;
    }
    
    .login-right {
      flex: 0 0 45%;
    }
  }

  @media (max-width: 1024px) {
    .login-page {
      flex-direction: column;
    }
    
    .login-left {
      flex: none;
      padding: 50px 30px;
    }
    
    .login-right {
      flex: none;
      padding: 30px;
    }
    
    .login-container {
      min-height: auto;
    }
    
    .brand-section {
      margin-bottom: 40px;
      
      .brand-title {
        font-size: 32px;
      }
    }
    
    .features-section {
      .feature-item {
        margin-bottom: 20px;
        padding: 20px;
      }
    }
  }

  @media (max-width: 768px) {
    .login-left {
      padding: 30px 15px;
    }
    
    .login-right {
      padding: 15px;
    }
    
    .login-card {
      padding: 30px 20px;
    }
    
    .brand-section {
      .brand-title {
        font-size: 24px;
      }
      
      .brand-subtitle {
        font-size: 14px;
      }
      
      .brand-description p {
        font-size: 14px;
      }
    }
    
    .login-header {
      .login-title {
        font-size: 24px;
      }
    }
  }

  @media (max-width: 480px) {
    .login-card {
      padding: 20px 15px;
    }
    
    .brand-section {
      .logo-wrapper .brand-logo {
        width: 80px;
        height: 80px;
      }
      
      .brand-title {
        font-size: 20px;
      }
    }
    
    .features-section {
      .feature-item {
        flex-direction: column;
        text-align: center;
        
        .feature-icon {
          margin-right: 0;
          margin-bottom: 15px;
        }
      }
    }
  }
</style>
