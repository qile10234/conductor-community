// Code generated by protoc-gen-go. DO NOT EDIT.
// source: model/polldata.proto

package model // import "github.com/netflix/conductor/client/gogrpc/conductor/model"

import proto "github.com/golang/protobuf/proto"
import fmt "fmt"
import math "math"

// Reference imports to suppress errors if they are not otherwise used.
var _ = proto.Marshal
var _ = fmt.Errorf
var _ = math.Inf

// This is a compile-time assertion to ensure that this generated file
// is compatible with the proto package it is being compiled against.
// A compilation error at this line likely means your copy of the
// proto package needs to be updated.
const _ = proto.ProtoPackageIsVersion2 // please upgrade the proto package

type PollData struct {
	QueueName            string   `protobuf:"bytes,1,opt,name=queue_name,json=queueName" json:"queue_name,omitempty"`
	Domain               string   `protobuf:"bytes,2,opt,name=domain" json:"domain,omitempty"`
	WorkerId             string   `protobuf:"bytes,3,opt,name=worker_id,json=workerId" json:"worker_id,omitempty"`
	LastPollTime         int64    `protobuf:"varint,4,opt,name=last_poll_time,json=lastPollTime" json:"last_poll_time,omitempty"`
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *PollData) Reset()         { *m = PollData{} }
func (m *PollData) String() string { return proto.CompactTextString(m) }
func (*PollData) ProtoMessage()    {}
func (*PollData) Descriptor() ([]byte, []int) {
	return fileDescriptor_polldata_a39ed67c311a83df, []int{0}
}
func (m *PollData) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_PollData.Unmarshal(m, b)
}
func (m *PollData) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_PollData.Marshal(b, m, deterministic)
}
func (dst *PollData) XXX_Merge(src proto.Message) {
	xxx_messageInfo_PollData.Merge(dst, src)
}
func (m *PollData) XXX_Size() int {
	return xxx_messageInfo_PollData.Size(m)
}
func (m *PollData) XXX_DiscardUnknown() {
	xxx_messageInfo_PollData.DiscardUnknown(m)
}

var xxx_messageInfo_PollData proto.InternalMessageInfo

func (m *PollData) GetQueueName() string {
	if m != nil {
		return m.QueueName
	}
	return ""
}

func (m *PollData) GetDomain() string {
	if m != nil {
		return m.Domain
	}
	return ""
}

func (m *PollData) GetWorkerId() string {
	if m != nil {
		return m.WorkerId
	}
	return ""
}

func (m *PollData) GetLastPollTime() int64 {
	if m != nil {
		return m.LastPollTime
	}
	return 0
}

func init() {
	proto.RegisterType((*PollData)(nil), "com.netflix.conductor.proto.PollData")
}

func init() { proto.RegisterFile("model/polldata.proto", fileDescriptor_polldata_a39ed67c311a83df) }

var fileDescriptor_polldata_a39ed67c311a83df = []byte{
	// 224 bytes of a gzipped FileDescriptorProto
	0x1f, 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0xff, 0x4c, 0x8f, 0x31, 0x4b, 0x03, 0x41,
	0x10, 0x46, 0x39, 0x23, 0x21, 0xb7, 0x88, 0xc5, 0x22, 0x72, 0x10, 0x84, 0x20, 0x16, 0xa9, 0x6e,
	0x0b, 0x3b, 0xcb, 0x60, 0xa1, 0x8d, 0x84, 0x60, 0x65, 0x73, 0xec, 0xed, 0x8e, 0xe7, 0xe2, 0xcc,
	0xce, 0xb9, 0xce, 0xa2, 0x3f, 0xc0, 0x1f, 0x2e, 0xb7, 0x89, 0x92, 0x72, 0xde, 0x2b, 0xe6, 0x7d,
	0xea, 0x82, 0xd8, 0x03, 0x9a, 0x91, 0x11, 0xbd, 0x15, 0xdb, 0x8e, 0x89, 0x85, 0xf5, 0xd2, 0x31,
	0xb5, 0x11, 0xe4, 0x15, 0xc3, 0x77, 0xeb, 0x38, 0xfa, 0xec, 0x84, 0xd3, 0x5e, 0x5e, 0xff, 0x54,
	0x6a, 0xb1, 0x65, 0xc4, 0x7b, 0x2b, 0x56, 0x5f, 0x29, 0xf5, 0x91, 0x21, 0x43, 0x17, 0x2d, 0x41,
	0x53, 0xad, 0xaa, 0x75, 0xbd, 0xab, 0x0b, 0x79, 0xb2, 0x04, 0xfa, 0x52, 0xcd, 0x3d, 0x93, 0x0d,
	0xb1, 0x39, 0x29, 0xea, 0x70, 0xe9, 0xa5, 0xaa, 0xbf, 0x38, 0xbd, 0x43, 0xea, 0x82, 0x6f, 0x66,
	0x45, 0x2d, 0xf6, 0xe0, 0xd1, 0xeb, 0x1b, 0x75, 0x8e, 0xf6, 0x53, 0xba, 0x29, 0xaa, 0x93, 0x40,
	0xd0, 0x9c, 0xae, 0xaa, 0xf5, 0x6c, 0x77, 0x36, 0xd1, 0xe9, 0xf3, 0x73, 0x20, 0xd8, 0x3c, 0x6c,
	0xd4, 0x5f, 0xc5, 0xb6, 0x7f, 0xb9, 0x1b, 0x82, 0xbc, 0xe5, 0xbe, 0x75, 0x4c, 0xe6, 0x10, 0x6e,
	0xfe, 0xc3, 0x8d, 0xc3, 0x00, 0x51, 0xcc, 0xc0, 0x43, 0x1a, 0xdd, 0x11, 0x2f, 0xe3, 0xfb, 0x79,
	0xd9, 0x75, 0xfb, 0x1b, 0x00, 0x00, 0xff, 0xff, 0x47, 0x36, 0x49, 0x42, 0x0c, 0x01, 0x00, 0x00,
}
