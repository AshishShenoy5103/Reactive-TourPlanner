import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Apollo, gql } from 'apollo-angular';

@Component({
  selector: 'app-admin-dashboard-admin-details',
  imports: [CommonModule],
  templateUrl: './admin-dashboard-admin-details.component.html',
  styleUrl: './admin-dashboard-admin-details.component.css'
})
export class AdminDashboardAdminDetailsComponent implements OnInit {
  admins: any[] = [];
  selectedAdmin: any = null;

  constructor(private http: HttpClient, private apollo: Apollo) {}

  ngOnInit() {
    const token = localStorage.getItem('authToken');
    const GET_ALL_ADMIN = gql`
      query GetAllAdmin {
        getAllAdmin {
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
          query: GET_ALL_ADMIN,
          context: {
            headers: new HttpHeaders({
              Authorization: `Bearer ${token}`
            })
          },
          fetchPolicy: 'no-cache'
        })
        .subscribe({
          next: (result: any) => {
            this.admins = result.data.getAllAdmin;
          },
          error: (err) => {
            console.error('Error fetching admins:', err);
          }
        });
    }
  }

  viewAdmin(admin: any) {
    this.selectedAdmin = admin;
  }

  closeModal() {
    this.selectedAdmin = null;
  }
}
