'use client';

interface DeleteConfirmModalProps {
  onClose: () => void;
  onConfirm: () => void;
}

export default function DeleteConfirmModal({ onClose, onConfirm }: DeleteConfirmModalProps) {
  return (
    <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50'>
      <div className='bg-white dark:bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4'>
        <div className='mb-6'>
          <div className='flex items-center gap-2 mb-4'>
            <svg
              className='w-6 h-6 text-red-500'
              fill='none'
              stroke='currentColor'
              viewBox='0 0 24 24'
              xmlns='http://www.w3.org/2000/svg'
            >
              <path
                strokeLinecap='round'
                strokeLinejoin='round'
                strokeWidth='2'
                d='M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z'
              />
            </svg>
            <h2 className='text-xl font-bold text-gray-900 dark:text-white'>모임 삭제 확인</h2>
          </div>
          <p className='text-gray-600 dark:text-gray-300 mb-4'>정말로 이 모임을 삭제하시겠습니까?</p>
          <div className='bg-yellow-50 dark:bg-yellow-900/30 border-l-4 border-yellow-400 p-4 mb-4'>
            <p className='text-yellow-800 dark:text-yellow-200 font-medium'>주의!</p>
            <p className='text-yellow-700 dark:text-yellow-300 text-sm'>
              현재 모임에 가입된 사람들의 멤버십이 모두 삭제됩니다.
            </p>
          </div>
        </div>
        <div className='flex justify-end gap-3'>
          <button
            onClick={onClose}
            className='px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 dark:bg-gray-700 dark:text-white dark:hover:bg-gray-600 transition-colors'
          >
            취소
          </button>
          <button
            onClick={onConfirm}
            className='px-4 py-2 bg-red-500 text-white rounded-md hover:bg-red-600 transition-colors'
          >
            삭제
          </button>
        </div>
      </div>
    </div>
  );
}
