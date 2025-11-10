import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/UserService';
import { Apollo, gql } from 'apollo-angular';

@Component({
  selector: 'app-admin-dashboard-user-details',
  imports: [CommonModule],
  templateUrl: './admin-dashboard-user-details.component.html',
  styleUrl: './admin-dashboard-user-details.component.css'
})
export class AdminDashboardUserDetailsComponent implements OnInit {
  users: any[] = [];
  selectedUser: any = null;

  constructor(private http: HttpClient, private userService: UserService, private apollo: Apollo) {}

  ngOnInit() {
    this.fetchUsers();

    this.userService.usersChanged$.subscribe(() => {
      this.fetchUsers();
    });
  }

  fetchUsers() {
    const token = localStorage.getItem('authToken');
    const GET_ALL_USER = gql`
    query GetAllUser {
      getAllUser {
        userId
        email
        userType
        createdAt
        firstName
        lastName
        aadharNumber
        city
        phoneNumber
      }
    }
  `;

  if (token) {
    this.apollo
      .query({
        query: GET_ALL_USER,
        context: {
          headers: new HttpHeaders({
              Authorization: `Bearer ${token}`
          })
        },
        fetchPolicy: 'no-cache'
      })
      .subscribe({
        next: (result: any) => {
          this.users = result.data.getAllUser;
        },
        error: (err) => {
          console.error('Error fetching users:', err);
        }
      });
  }
  }

  viewUser(user: any) {
    this.selectedUser = user;
  }

  closeModal() {
    this.selectedUser = null;
  }
}
