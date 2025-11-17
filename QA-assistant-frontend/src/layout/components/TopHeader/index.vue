<template>
  <div class="top-header">
    <!-- 1. Logo区域 -->
    <div class="top-header-logo">
      <img :src="websiteConfig.logo" alt="" />
      <h2 class="logo-title">{{ websiteConfig.title }}</h2>
    </div>
    
    <!-- 2. 一级菜单区域 -->
    <div class="top-header-menu">
      <div class="menu-item" 
           v-for="menu in primaryMenus" 
           :key="menu.key"
           :class="{ active: activeMenu === menu.key }"
           @click="handleMenuClick(menu)">
        {{ menu.label }}
      </div>
    </div>
    
    <!-- 3. 设置和用户信息区域 -->
    <div class="top-header-right">
      <!-- 锁屏 -->
      <div class="header-action" @click="handleLockScreen">
        <n-tooltip placement="bottom">
          <template #trigger>
            <n-icon size="18" color="white">
              <LockOutlined />
            </n-icon>
          </template>
          <span>锁屏</span>
        </n-tooltip>
      </div>
      
      <!-- 全屏切换 -->
      <div class="header-action" @click="toggleFullScreen">
        <n-tooltip placement="bottom">
          <template #trigger>
            <n-icon size="18" color="white">
              <component :is="fullscreenIcon" />
            </n-icon>
          </template>
          <span>全屏</span>
        </n-tooltip>
      </div>
      
      <!-- 设置 -->
      <div class="header-action" @click="openSetting">
        <n-tooltip placement="bottom">
          <template #trigger>
            <n-icon size="18" color="white">
              <SettingOutlined />
            </n-icon>
          </template>
          <span>设置</span>
        </n-tooltip>
      </div>
      
      <!-- 用户信息 -->
      <div class="header-user">
        <n-dropdown trigger="hover" @select="avatarSelect" :options="avatarOptions">
          <div class="user-info">
            <n-avatar :src="websiteConfig.logo" size="small">
              <template #icon>
                <UserOutlined />
              </template>
            </n-avatar>
            <span class="username">{{ username }}</span>
          </div>
        </n-dropdown>
      </div>
    </div>
  </div>
  <!--项目配置-->
  <ProjectSetting ref="drawerSetting" />
</template>

<script lang="ts">
  import { websiteConfig } from '@/config/website.config';
  import { useUserStore } from '@/store/modules/user';
  import { useScreenLockStore } from '@/store/modules/screenLock';
  import { NDialogProvider, useDialog, useMessage } from 'naive-ui';
  import { defineComponent, reactive, ref, toRefs } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { SettingOutlined, UserOutlined, FullscreenOutlined, FullscreenExitOutlined, LockOutlined } from '@vicons/antd';
  import ProjectSetting from '../Header/ProjectSetting.vue';

  export default defineComponent({
    name: 'TopHeader',
    components: { 
      NDialogProvider, 
      ProjectSetting,
      SettingOutlined,
      UserOutlined,
      FullscreenOutlined,
      FullscreenExitOutlined,
      LockOutlined
    },
    setup() {
      const userStore = useUserStore();
      const useLockscreen = useScreenLockStore();
      const message = useMessage();
      const dialog = useDialog();
      const router = useRouter();
      const route = useRoute();

      const drawerSetting = ref();

      const state = reactive({
        username: userStore?.info?.username ?? '',
        fullscreenIcon: 'FullscreenOutlined',
        activeMenu: 'production', // 默认激活的菜单
      });

      // 一级菜单数据
      const primaryMenus = [
        { key: 'production', label: '生产运行' },
        { key: 'monitor', label: '大监控管理' },
        { key: 'emergency', label: '应急协调' },
        { key: 'support', label: '辅助保障' },
        { key: 'safety', label: '油气流态势监测' },
        { key: 'equipment', label: '设备管理' },
        { key: 'development', label: '开发规范' }
      ];

      // 切换全屏图标
      const toggleFullscreenIcon = () =>
        (state.fullscreenIcon =
          document.fullscreenElement !== null ? 'FullscreenExitOutlined' : 'FullscreenOutlined');

      // 监听全屏切换事件
      document.addEventListener('fullscreenchange', toggleFullscreenIcon);

      // 全屏切换
      const toggleFullScreen = () => {
        if (!document.fullscreenElement) {
          document.documentElement.requestFullscreen();
        } else {
          if (document.exitFullscreen) {
            document.exitFullscreen();
          }
        }
      };

      // 退出登录
      const doLogout = () => {
        dialog.info({
          title: '提示',
          content: '您确定要退出登录吗',
          positiveText: '确定',
          negativeText: '取消',
          onPositiveClick: () => {
            userStore.logout().then(() => {
              message.success('成功退出登录');
              router
                .replace({
                  name: 'Login',
                  query: {
                    redirect: route.fullPath,
                  },
                })
                .finally(() => location.reload());
            });
          },
          onNegativeClick: () => {},
        });
      };

      const avatarOptions = [
        {
          label: '个人设置',
          key: 1,
        },
        {
          label: '退出登录',
          key: 2,
        },
      ];

      //头像下拉菜单
      const avatarSelect = (key) => {
        switch (key) {
          case 1:
            router.push({ name: 'Setting' });
            break;
          case 2:
            doLogout();
            break;
        }
      };

      function openSetting() {
        const { openDrawer } = drawerSetting.value;
        openDrawer();
      }

      // 处理一级菜单点击
      function handleMenuClick(menu) {
        state.activeMenu = menu.key;
        
        // 根据菜单key进行路由跳转
        switch (menu.key) {
          case 'production':
            // 生产运行 - 跳转到默认首页
            router.push('/');
            break;
          case 'monitor':
            // 大监控管理 - 跳转到监控模块
            router.push('/monitor');
            break;
          case 'emergency':
            // 应急协调 - 跳转到应急协调模块
            router.push('/emergency');
            break;
          case 'support':
            // 辅助保障 - 跳转到辅助保障模块
            router.push('/support');
            break;
          case 'safety':
            // 油气流态势监测 - 跳转到安全监测模块
            router.push('/safety');
            break;
          case 'equipment':
            // 设备管理 - 跳转到EQMS模块
            router.push('/eqms');
            break;
          case 'development':
            // 开发规范 - 跳转到开发规范模块
            router.push('/dev-standards');
            break;
          default:
            console.log('点击菜单:', menu.label);
        }
      }

      // 处理锁屏
      function handleLockScreen() {
        useLockscreen.setLock(true);
      }

      return {
        ...toRefs(state),
        primaryMenus,
        handleMenuClick,
        handleLockScreen,
        toggleFullScreen,
        doLogout,
        route,
        avatarOptions,
        avatarSelect,
        drawerSetting,
        openSetting,
        websiteConfig,
      };
    },
  });
</script>

<style lang="less" scoped>
  .top-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    height: 56px;
    background-color: #0080EB;
    box-shadow: 0 1px 4px rgb(0 21 41 / 8%);
    transition: all 0.2s ease-in-out;
    width: 100%;
    color: white;

    // Logo区域
    .top-header-logo {
      display: flex;
      align-items: center;
      min-width: 200px;

      img {
        width: auto;
        height: 32px;
        margin-right: 12px;
        transition: all 0.3s ease;
      }

      .logo-title {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: white;
        transition: all 0.3s ease;
      }

      &:hover img {
        transform: scale(1.05);
      }

      &:hover .logo-title {
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
      }
    }

    // 一级菜单区域
    .top-header-menu {
      display: flex;
      align-items: center;
      flex: 1;
      justify-content: flex-start;
      gap: 24px;
      padding-left: 20px;
       height: 100%; 
      .menu-item {
        height: 100%;
        padding: 0 16px;
        display: flex;
        align-items: center;
        transition: all 0.3s ease;
        font-size: 15px;
        font-weight: 500;
        color: rgba(255, 255, 255, 0.9);
        cursor: pointer;
        position: relative;

        &:hover {
          background-color: rgba(255, 255, 255, 0.1);
          color: white;
          transform: translateY(-1px);
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }

        &.active {
          background-color: rgba(255, 255, 255, 0.15);
          color: white;
          font-weight: 600;
        }
      }
    }

    // 右侧区域
    .top-header-right {
      display: flex;
      align-items: center;
      gap: 8px;
      min-width: 200px;
      justify-content: flex-end;

      .header-action {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 40px;
        height: 40px;
        cursor: pointer;
        border-radius: 4px;
        transition: all 0.3s ease;

        &:hover {
          background-color: rgba(255, 255, 255, 0.1);
          transform: scale(1.05);
        }
      }

      .header-user {
        .user-info {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 8px 12px;
          cursor: pointer;
          border-radius: 4px;
          transition: all 0.3s ease;

          &:hover {
            background-color: rgba(255, 255, 255, 0.1);
            transform: translateY(-1px);
          }

          .username {
            color: white;
            font-size: 14px;
          }
        }
      }
    }
  }

  // 响应式设计
  @media (max-width: 768px) {
    .top-header {
      padding: 0 10px;

      .top-header-logo {
        min-width: 150px;

        .logo-title {
          font-size: 16px;
        }
      }

      .top-header-menu {
        gap: 16px;
        padding-left: 15px;

        .menu-item {
          padding: 0 12px;
          font-size: 14px;
        }
      }

      .top-header-right {
        gap: 6px;
        min-width: 150px;

        .header-action {
          width: 36px;
          height: 36px;
        }
      }
    }
  }
</style>
