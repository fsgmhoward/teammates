import { Component, EventEmitter, Input, Output } from '@angular/core';
import { SimpleModalService } from '../../../../services/simple-modal.service';
import { TimezoneService } from '../../../../services/timezone.service';
import { ApiConst } from '../../../../types/api-const';
import { NotificationTargetUser, NotificationType } from '../../../../types/api-request';
import { SimpleModalType } from '../../../components/simple-modal/simple-modal-type';
import { NotificationEditFormMode, NotificationEditFormModel } from './notification-edit-form-model';

@Component({
  selector: 'tm-notification-edit-form',
  templateUrl: './notification-edit-form.component.html',
  styleUrls: ['./notification-edit-form.component.scss'],
})
export class NotificationEditFormComponent {

  NotificationEditFormMode = NotificationEditFormMode;
  NotificationType = NotificationType;
  NotificationTargetUser = NotificationTargetUser;

  NOTIFICATION_TITLE_MAX_LENGTH = ApiConst.NOTIFICATION_TITLE_MAX_LENGTH;

  guessTimezone = '';

  @Input()
  model: NotificationEditFormModel = {
    notificationId: '',
    shown: false,

    startTime: { hour: 0, minute: 0 },
    startDate: { year: 0, month: 0, day: 0 },
    endTime: { hour: 0, minute: 0 },
    endDate: { year: 0, month: 0, day: 0 },

    type: NotificationType.MAINTENANCE,
    targetUser: NotificationTargetUser.GENERAL,

    title: '',
    message: '',

    isSaving: false,
    isEditable: true,
    isDeleting: false,
  };

  @Output()
  modelChange: EventEmitter<NotificationEditFormModel> = new EventEmitter();

  @Input()
  formMode: NotificationEditFormMode = NotificationEditFormMode.ADD;

  @Input()
  courseCandidates: Notification[] = [];

  // event emission
  @Output()
  addNewNotificationEvent = new EventEmitter<void>();

  @Output()
  editExistingNotificationEvent = new EventEmitter<void>();

  @Output()
  cancelEditingNotificationEvent = new EventEmitter<void>();

  @Output()
  deleteExistingNotificationEvent = new EventEmitter<void>();

  @Output()
  closeEditFormEvent = new EventEmitter<void>();

  constructor(
    timezoneService: TimezoneService,
    private simpleModalService: SimpleModalService,
  ) {
    this.guessTimezone = timezoneService.guessTimezone();
  }

  /**
   * Triggers the change of the model for the form.
   */
  triggerModelChange(field: string, data: any): void {
    this.modelChange.emit({
      ...this.model,
      [field]: data,
    });
  }

  /**
   * Handles submit button click event.
   */
  submitFormHandler(): void {
    // resolve local date time to timestamp
    if (this.formMode === NotificationEditFormMode.ADD) {
      this.addNewNotificationEvent.emit();
    }

    if (this.formMode === NotificationEditFormMode.EDIT) {
      this.editExistingNotificationEvent.emit();
    }
  }

  /**
   * Handles cancel button click event.
   */
  cancelHandler(): void {
    this.simpleModalService.openConfirmationModal('Discard unsaved edit?',
        SimpleModalType.WARNING, 'Warning: Any unsaved changes will be lost.').result.then(() => {
          this.cancelEditingNotificationEvent.emit();
        }, () => {});
  }

  /**
   * Handles delete current feedback session button click event.
   */
  deleteHandler(): void {
    this.simpleModalService.openConfirmationModal(
        `Delete the notification <strong>${this.model.title}</strong>?`,
        SimpleModalType.WARNING,
        'This action is not reversible and the delete will be permanent.',
    ).result.then(() => {
      this.deleteExistingNotificationEvent.emit();
    }, () => {});
  }

  /**
   * Handles closing of the edit form.
   */
  closeEditFormHandler(): void {
    this.closeEditFormEvent.emit();
  }

}
