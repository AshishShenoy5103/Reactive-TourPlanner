import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Apollo } from 'apollo-angular';
import { gql } from '@apollo/client/core';

@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  firstName = '';
  lastName = '';
  email = '';
  phoneNumber = '';
  city = '';
  aadharNumber = '';
  createdAt: string | null = null;
  initials = '';

  constructor(private http: HttpClient, private apollo: Apollo) {}

  ngOnInit() {
    const token = localStorage.getItem('authToken');

    const GET_CURRENT_ADMIN_PROFILE = gql`
      query GetCurrentAdminProfile {
        getCurrentAdminProfile {
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
          query: GET_CURRENT_ADMIN_PROFILE,
          context: {
            headers: new HttpHeaders({
              Authorization: `Bearer ${token}`
            })
          },
          fetchPolicy: 'no-cache'
        })
        .subscribe({
          next: (result: any) => {
            const data = result.data.getCurrentAdminProfile;
            this.firstName = data.firstName;
            this.lastName = data.lastName;
            this.email = data.email;
            this.phoneNumber = data.phoneNumber;
            this.city = data.city;
            this.aadharNumber = data.aadharNumber;
            this.createdAt = data.createdAt;
            this.initials =
              (this.firstName?.charAt(0) || '') +
              (this.lastName?.charAt(0) || '');
          },
          error: (err) => {
            console.error('Error fetching admin profile:', err);
          }
        });
    }
  }
}
