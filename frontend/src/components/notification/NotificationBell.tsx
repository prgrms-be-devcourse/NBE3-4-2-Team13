import { Bell } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useState, useEffect } from "react";
import { notificationService } from "@/services/notificationService";

// 1. 인터페이스 수정
interface Notification {
  id: number;           
  userId: string;       
  title: string;
  content: string;
  createdAt: string;
  read: boolean;
  type?: string;        
  targetId?: number;    
}

export function NotificationBell() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isLoading, setIsLoading] = useState(false);

  // SSE 리스너 추가
  useEffect(() => {
    const handleNewNotification = (notification: Notification) => {
      console.log('NotificationBell: 새 알림 수신:', notification);
      setNotifications(prev => {
        console.log('이전 알림 목록:', prev);
        return [notification, ...prev];
      });
      setUnreadCount(prev => prev + 1);
    };

    console.log('NotificationBell: 리스너 등록');
    notificationService.addNotificationListener(handleNewNotification);
    
    // SSE 연결 시작
    notificationService.connect();

    // 컴포넌트 언마운트 시 정리
    return () => {
      console.log('NotificationBell: 리스너 제거');
      notificationService.removeNotificationListener(handleNewNotification);
      notificationService.disconnect();
    };
  }, []);

  // 초기 알림 목록 로드
  useEffect(() => {
    const loadNotifications = async () => {
      try {
        setIsLoading(true);
        const notifs = await notificationService.getNotifications();
        setNotifications(notifs);
        setUnreadCount(notifs.filter((n: Notification) => !n.read).length);
      } catch (error) {
        console.error('알림 로드 중 오류:', error);
      } finally {
        setIsLoading(false);
      }
    };

    loadNotifications();
  }, []);

  // 알림 목록 조회 함수
  const fetchNotifications = async () => {
    try {
      setIsLoading(true);
      const notifs = await notificationService.getNotifications();
      setNotifications(notifs);
      setUnreadCount(notifs.filter((n: Notification) => !n.read).length);
    } catch (error) {
      console.error('알림 목록 조회 중 오류:', error);
    } finally {
      setIsLoading(false);
    }
  };

  // 3. 읽음 처리 함수 수정 - notificationId 사용
  const handleMarkAsRead = async (notificationId: number) => {
    try {
      console.log('읽음 처리할 알림 ID:', notificationId);
      const token = localStorage.getItem('accessToken');
      const response = await fetch(`http://localhost:8080/api/v1/notifications/${notificationId}/read`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      console.log('서버 응답:', response);

      setNotifications(notifications.map(n =>
        n.id === notificationId ? { ...n, read: true } : n
      ));
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (error) {
      console.error('알림 읽음 처리 중 오류:', error);
    }
  };

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild onClick={fetchNotifications}>
        <Button variant="ghost" size="icon" className="relative">
          <Bell className="h-5 w-5" />
          {unreadCount > 0 && (
            <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
              {unreadCount}
            </span>
          )}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-80">
        {isLoading ? (
          <div className="p-4 text-center text-muted-foreground">
            알림 로딩중...
          </div>
        ) : notifications.length === 0 ? (
          <div className="p-4 text-center text-muted-foreground">
            새로운 알림이 없습니다
          </div>
        ) : (
          notifications.map((notification) => (
            <DropdownMenuItem
              key={`${notification.id}-${notification.createdAt}`}  // 고유한 key 값 설정
              className={`p-4 cursor-pointer ${!notification.read ? 'bg-muted/50' : ''}`}
              onClick={() => handleMarkAsRead(notification.id)}
            >
              <div>
                <div className="font-semibold">{notification.title}</div>
                <div className="text-sm text-muted-foreground">{notification.content}</div>
                <div className="text-xs text-muted-foreground mt-1">
                  {new Date(notification.createdAt).toLocaleString()}
                </div>
              </div>
            </DropdownMenuItem>
          ))
        )}
      </DropdownMenuContent>
    </DropdownMenu>
  );
} 