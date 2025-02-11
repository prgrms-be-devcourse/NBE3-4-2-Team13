'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Pagination from '@/components/common/Pagination';
import ConfirmModal from '@/components/common/ConfirmModal';

interface MeetingApplicationDto {
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
}

export default function MeetingApplicationsPage({ groupId }: Props) {
  const router = useRouter();
  const [applications, setApplications] = useState<MeetingApplicationDto[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [modalConfig, setModalConfig] = useState({
    title: '',
    message: '',
    onConfirm: () => {},
  });

  const fetchApplications = async () => {
    try {
      setIsLoading(true);
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setError('로그인이 필요합니다.');
        return;
      }

      const response = await fetch(
        `http://localhost:8080/api/v1/groups/${groupId}/meeting_applications?page=${currentPage - 1}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        }
      );

      const data = await response.json();
      if (data.isSuccess) {
        setApplications(data.data.content);
        setTotalPages(data.data.totalPages);
        setTotalElements(data.data.totalElements);
      } else {
        setError(data.message || '신청 목록을 불러오는데 실패했습니다.');
      }
    } catch (error) {
      setError('신청 목록을 불러오는데 실패했습니다.');
      console.error('Failed to fetch applications:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleApprove = async (memberId: number, isAccept: boolean) => {
    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch(`http://localhost:8080/api/v1/groups/${groupId}/approve`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          memberId,
          isAccept,
        }),
      });

      const data = await response.json();
      if (data.isSuccess) {
        fetchApplications();
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
    fetchApplications();
  }, [groupId, currentPage]);

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

  return (
    <div className='container mx-auto px-4 py-8'>
      <h1 className='text-2xl font-bold mb-6'>모임 신청 목록</h1>

      {applications.length === 0 ? (
        <div className='text-center py-8 text-gray-600 dark:text-gray-400'>신청 내역이 없습니다.</div>
      ) : (
        <div className='space-y-4'>
          {applications.map((application) => (
            <div
              key={application.id}
              className='bg-white dark:bg-gray-800 rounded-lg shadow p-4 flex items-center justify-between'
            >
              <div
                className='flex-1 cursor-pointer'
                onClick={() => router.push(`/groups/${groupId}/meeting_applications/${application.id}`)}
              >
                <div className='flex items-center gap-4 mb-2'>
                  <span className='text-sm text-gray-600 dark:text-gray-400'>신청자: {application.nickname}</span>
                  <span className='text-sm text-gray-600 dark:text-gray-400'>신청일: {application.createdAt}</span>
                </div>
                <p className='text-gray-800 dark:text-gray-200'>
                  {application.content.length > 100
                    ? `${application.content.substring(0, 100)}...`
                    : application.content}
                </p>
              </div>

              <div className='flex items-center gap-2 ml-4'>
                {!application.rejected && !application.isMember ? (
                  <>
                    <button
                      onClick={() =>
                        showConfirmModal('신청 승인', '이 신청을 승인하시겠습니까?', () =>
                          handleApprove(application.memberId, true)
                        )
                      }
                      className='px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors'
                    >
                      승인
                    </button>
                    <button
                      onClick={() =>
                        showConfirmModal('신청 거절', '이 신청을 거절하시겠습니까?', () =>
                          handleApprove(application.memberId, false)
                        )
                      }
                      className='px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors'
                    >
                      거절
                    </button>
                  </>
                ) : (
                  <>
                    {application.rejected && (
                      <span className='px-3 py-1.5 rounded-full text-sm bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-100'>
                        거절됨
                      </span>
                    )}
                    {application.isMember && (
                      <span
                        className={`px-3 py-1.5 rounded-full text-sm ${
                          application.isAdmin
                            ? 'bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-100'
                            : 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-100'
                        }`}
                      >
                        {application.isAdmin ? '관리자' : '멤버'}
                      </span>
                    )}
                  </>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {applications.length > 0 && (
        <div className='mt-6'>
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            totalElements={totalElements}
            onPageChange={setCurrentPage}
          />
        </div>
      )}

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
