'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import ConfirmModal from '@/components/common/ConfirmModal';

interface MeetingApplication {
  id: number;
  groupId: number;
  memberId: number;
  nickname: string;
  content: string;
  createdAt: string;
  rejected: boolean;
  isMember: boolean;
  isAdmin: boolean;
}

interface Props {
  groupId: string;
  meetingApplicationId: string;
}

export default function MeetingApplicationDetailPage({ groupId, meetingApplicationId }: Props) {
  const router = useRouter();
  const [application, setApplication] = useState<MeetingApplication | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [modalConfig, setModalConfig] = useState({
    title: '',
    message: '',
    onConfirm: () => {},
  });

  const fetchApplication = async () => {
    try {
      if (!groupId || !meetingApplicationId) {
        setError('잘못된 접근입니다.');
        return;
      }

      setIsLoading(true);
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setError('로그인이 필요합니다.');
        return;
      }

      const response = await fetch(
        `http://localhost:8080/api/v1/groups/${groupId}/meeting_applications/${meetingApplicationId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        }
      );

      const data = await response.json();
      if (data.isSuccess) {
        setApplication(data.data);
      } else {
        setError(data.message || '신청 내용을 불러오는데 실패했습니다.');
      }
    } catch (error) {
      setError('신청 내용을 불러오는데 실패했습니다.');
      console.error('Failed to fetch application:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleApprove = async (isAccept: boolean) => {
    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch(`http://localhost:8080/api/v1/groups/${groupId}/approve`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          memberId: application?.memberId,
          isAccept,
        }),
      });

      const data = await response.json();
      if (data.isSuccess) {
        router.push(`/groups/${groupId}/meeting_applications`);
      }
    } catch (error) {
      console.error('Failed to process application:', error);
    }
  };

  const showConfirmModal = (title: string, message: string, onConfirm: () => void) => {
    setModalConfig({ title, message, onConfirm });
    setShowModal(true);
  };

  useEffect(() => {
    if (groupId && meetingApplicationId) {
      fetchApplication();
    }
  }, [groupId, meetingApplicationId]);

  if (isLoading) {
    return (
      <div className='container mx-auto px-4 py-8'>
        <div className='text-center'>로딩 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className='container mx-auto px-4 py-8'>
        <div className='text-red-600'>{error}</div>
      </div>
    );
  }

  if (!application) {
    return (
      <div className='container mx-auto px-4 py-8'>
        <div className='text-center'>신청 내용을 찾을 수 없습니다.</div>
      </div>
    );
  }

  return (
    <div className='container mx-auto px-4 py-8'>
      <div className='bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6'>
        <div className='mb-6'>
          <h1 className='text-2xl font-bold mb-4'>모임 신청 상세</h1>
          <div className='flex justify-between items-center'>
            <div className='flex gap-4 text-sm text-gray-600 dark:text-gray-400'>
              <span>신청자: {application.nickname}</span>
              <span>신청일: {application.createdAt}</span>
            </div>
            <div>
              {application.rejected ? (
                <span className='px-3 py-1.5 rounded-full text-sm bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-100'>
                  거절됨
                </span>
              ) : application.isMember ? (
                <span
                  className={`px-3 py-1.5 rounded-full text-sm ${
                    application.isAdmin
                      ? 'bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-100'
                      : 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-100'
                  }`}
                >
                  {application.isAdmin ? '관리자' : '멤버'}
                </span>
              ) : null}
            </div>
          </div>
        </div>

        <div className='mb-8'>
          <h2 className='text-lg font-semibold mb-2'>신청 내용</h2>
          <p className='whitespace-pre-wrap text-gray-800 dark:text-gray-200'>{application.content}</p>
        </div>

        {!application.rejected && !application.isMember && (
          <div className='flex gap-4'>
            <button
              onClick={() => showConfirmModal('신청 승인', '이 신청을 승인하시겠습니까?', () => handleApprove(true))}
              className='px-6 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors'
            >
              승인
            </button>
            <button
              onClick={() => showConfirmModal('신청 거절', '이 신청을 거절하시겠습니까?', () => handleApprove(false))}
              className='px-6 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors'
            >
              거절
            </button>
          </div>
        )}
      </div>

      {showModal && (
        <ConfirmModal
          title={modalConfig.title}
          message={modalConfig.message}
          onConfirm={() => {
            modalConfig.onConfirm();
            setShowModal(false);
          }}
          onCancel={() => setShowModal(false)}
        />
      )}
    </div>
  );
}
