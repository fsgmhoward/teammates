import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ResourceEndpoints } from '../types/api-const';
import { Notification, Notifications } from '../types/api-output';
import { CreateNotificationRequest } from '../types/api-request';
import { HttpRequestService } from './http-request.service';

/**
 * Handles notification related logic injection
 */
@Injectable({
  providedIn: 'root',
})
export class NotificationService {

  constructor(private httpRequestService: HttpRequestService) { }

  /**
   * Creates a notification by calling API.
   */
  createNotification(request: CreateNotificationRequest): Observable<Notification> {
    return this.httpRequestService.post(ResourceEndpoints.NOTIFICATION, {}, request);
  }

  /**
   * Retrieve all notifications by calling API.
   */
  getNotifications(): Observable<Notifications> {
    return this.httpRequestService.get(ResourceEndpoints.NOTIFICATION);
  }
}
